import cv2
import numpy as np
import math

def detect_and_warp_red_ring(image):
    """
    Wykrywa czerwoną obręcz w obrazie i przeprowadza transformację, aby obręcz była kołem.

    Args:
        image: Obraz wejściowy w formacie BGR

    Returns:
        Obraz z wyprostowaną obręczą, maska obręczy i jej środek
    """
    # Kopia oryginału
    original = image.copy()

    # Konwersja do HSV
    hsv = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)

    # Definicja zakresu koloru czerwonego w HSV
    lower_red1 = np.array([0, 100, 100])
    upper_red1 = np.array([10, 255, 255])
    lower_red2 = np.array([160, 100, 100])
    upper_red2 = np.array([180, 255, 255])

    # Tworzenie masek dla czerwonego koloru
    mask1 = cv2.inRange(hsv, lower_red1, upper_red1)
    mask2 = cv2.inRange(hsv, lower_red2, upper_red2)
    red_mask = cv2.bitwise_or(mask1, mask2)

    # Usunięcie szumów za pomocą morfologii
    kernel = np.ones((5, 5), np.uint8)
    red_mask = cv2.morphologyEx(red_mask, cv2.MORPH_OPEN, kernel)
    red_mask = cv2.morphologyEx(red_mask, cv2.MORPH_CLOSE, kernel)

    # Znajdowanie konturów
    contours, _ = cv2.findContours(red_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    if not contours:
        print("Nie znaleziono czerwonych obiektów")
        return None, None, None

    # Wybierz największy kontur (prawdopodobnie obręcz)
    largest_contour = max(contours, key=cv2.contourArea)

    # Oblicz elipsę najlepiej pasującą do konturu
    ellipse = cv2.fitEllipse(largest_contour)
    center, axes, angle = ellipse

    # Oblicz macierz transformacji afinicznej na podstawie elipsy
    # Przekształcamy elipsę na koło
    scale_x = axes[0] / axes[1] if axes[0] > axes[1] else 1.0
    scale_y = axes[1] / axes[0] if axes[1] > axes[0] else 1.0

    # Obrót o kąt elipsy
    cos_angle = math.cos(math.radians(angle))
    sin_angle = math.sin(math.radians(angle))

    # Macierz rotacji
    rot_mat = np.array([
        [cos_angle, -sin_angle],
        [sin_angle, cos_angle]
    ])

    # Macierz skalowania
    scale_mat = np.array([
        [scale_x, 0],
        [0, scale_y]
    ])

    # Macierz rotacji z powrotem
    rot_back_mat = np.array([
        [cos_angle, sin_angle],
        [-sin_angle, cos_angle]
    ])

    # Złożenie transformacji: obrót -> skalowanie -> obrót z powrotem
    trans_mat = np.dot(np.dot(rot_mat, scale_mat), rot_back_mat)

    # Tworzenie macierzy transformacji afinicznej 2x3
    affine_mat = np.zeros((2, 3), dtype=np.float32)
    affine_mat[:, :2] = trans_mat

    # Centrowanie obrazu
    target_center_x = image.shape[1] // 2
    target_center_y = image.shape[0] // 2

    affine_mat[0, 2] = target_center_x - center[0]
    affine_mat[1, 2] = target_center_y - center[1]

    # Wykonaj transformację afiniczną
    warped_image = cv2.warpAffine(image, affine_mat, (image.shape[1], image.shape[0]))
    warped_mask = cv2.warpAffine(red_mask, affine_mat, (red_mask.shape[1], red_mask.shape[0]))

    # Nowy środek po transformacji
    new_center = (target_center_x, target_center_y)

    return warped_image, warped_mask, new_center

def detect_black_shapes(image, red_ring_mask, center):
    """
    Wykrywa czarne kształty wewnątrz czerwonej obręczy.

    Args:
        image: Obraz wejściowy
        red_ring_mask: Maska czerwonej obręczy
        center: Środek obręczy

    Returns:
        Lista konturów czarnych kształtów i obraz z zaznaczonymi kształtami
    """
    # Konwersja do skali szarości
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    # Progowanie - wykrywanie ciemnych obszarów
    _, black_mask = cv2.threshold(gray, 20, 255, cv2.THRESH_BINARY_INV)

    # Stwórz maskę obszaru wewnątrz czerwonej obręczy
    # Najpierw znajdź kontury czerwonej obręczy
    contours, _ = cv2.findContours(red_ring_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    if not contours:
        print("Nie można znaleźć konturu czerwonej obręczy")
        return [], image

    # Utwórz pustą maskę
    inside_mask = np.zeros_like(red_ring_mask)

    # Wypełnij wnętrze konturu
    cv2.drawContours(inside_mask, contours, -1, 255, -1)

    # Zastosuj maskę do czarnych kształtów
    black_inside_mask = cv2.bitwise_and(black_mask, inside_mask)

    # Morfologiczne operacje aby usunąć szumy
    kernel = np.ones((3, 3), np.uint8)
    black_inside_mask = cv2.morphologyEx(black_inside_mask, cv2.MORPH_OPEN, kernel)

    # Znajdź kontury czarnych kształtów
    black_contours, _ = cv2.findContours(black_inside_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    # Narysuj kontury na obrazie
    result_image = image.copy()
    cv2.drawContours(result_image, black_contours, -1, (0, 255, 0), 2)

    # Narysuj środek obręczy
    cv2.circle(result_image, (int(center[0]), int(center[1])), 5, (0, 0, 255), -1)

    return black_contours, result_image

def calculate_hu_moments(contour):
    """
    Oblicza niezmienniki momentowe Hu dla danego konturu.

    Args:
        contour: Kontur kształtu

    Returns:
        Wektor cech log10(abs(wartość) + 1e-10)
    """
    # Oblicz momenty
    moments = cv2.moments(contour)

    # Oblicz niezmienniki Hu
    hu_moments = cv2.HuMoments(moments)

    # Transformacja logarytmiczna: log10(abs(wartość) + 1e-10)
    log_hu_moments = -np.sign(hu_moments) * np.log10(np.abs(hu_moments) + 1e-10)

    return log_hu_moments.flatten()

def main():
    # Wczytaj obraz
    image_path = r"C:\Users\aleks\Desktop\30znak.png"  # Zmień na swoją ścieżkę do obrazu
    image = cv2.imread(image_path)

    if image is None:
        print(f"Nie można wczytać obrazu: {image_path}")
        return

    # Wykryj i prostuj czerwoną obręcz
    warped_image, ring_mask, center = detect_and_warp_red_ring(image)

    if warped_image is None:
        print("Nie udało się przeprowadzić warpingu czerwonej obręczy")
        return

    # Zapisz wyprostowany obraz
    cv2.imwrite("warped_image.jpg", warped_image)

    # Wykryj czarne kształty na oryginalnym obrazie
    black_contours_orig, result_image_orig = detect_black_shapes(image, ring_mask, center)

    # Wykryj czarne kształty na zwarpowanym obrazie
    black_contours, result_image = detect_black_shapes(warped_image, ring_mask, center)

    # Zapisz obrazy z zaznaczonymi kształtami
    cv2.imwrite("detected_shapes_original.jpg", result_image_orig)
    cv2.imwrite("detected_shapes_warped.jpg", result_image)

    print(f"Znaleziono {len(black_contours)} czarnych kształtów na zwarpowanym obrazie")

    # Oblicz niezmienniki momentowe dla każdego kształtu
    shape_features = []
    for i, contour in enumerate(black_contours):
        # Oblicz niezmienniki momentowe
        hu_features = calculate_hu_moments(contour)

        # Dodaj do listy
        shape_features.append(hu_features)

        # Wypisz wyniki
        print(f"Kształt {i+1}:")
        print(f"  Niezmienniki momentowe Hu (log10): {hu_features}")

        # Narysuj numer kształtu na obrazie wynikowym
        M = cv2.moments(contour)
        if M["m00"] != 0:
            cx = int(M["m10"] / M["m00"])
            cy = int(M["m01"] / M["m00"])
            cv2.putText(result_image, str(i+1), (cx, cy), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (255, 0, 0), 2)

    # Zapisz ponownie obraz z numerami kształtów
    cv2.imwrite("numbered_shapes.jpg", result_image)

    # Zapisz wektory cech do pliku
    with open("shape_features.txt", "w") as f:
        for i, features in enumerate(shape_features):
            f.write(f"Kształt {i+1}: {features.tolist()}\n")

    # Przygotuj okno z wszystkimi obrazami
    # Określ rozmiar okna
    window_width = image.shape[1] * 2
    window_height = image.shape[0] * 2

    # Utwórz pustą ramkę
    display_frame = np.zeros((window_height, window_width, 3), dtype=np.uint8)

    # Umieść obrazy w ramce
    display_frame[0:image.shape[0], 0:image.shape[1]] = image  # Oryginalny obraz
    display_frame[0:image.shape[0], image.shape[1]:image.shape[1]*2] = result_image_orig  # Oryginalny z konturami
    display_frame[image.shape[0]:image.shape[0]*2, 0:image.shape[1]] = warped_image  # Zwarpowany obraz
    display_frame[image.shape[0]:image.shape[0]*2, image.shape[1]:image.shape[1]*2] = result_image  # Zwarpowany z konturami

    text_color = (100, 100, 100)  # Szary kolor dla tekstu
    # Dodaj opisy
    cv2.putText(display_frame, "Oryginalny obraz", (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, text_color, 2)
    cv2.putText(display_frame, "Oryginalny z wykrytymi ksztaltami", (image.shape[1] + 10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, text_color, 2)
    cv2.putText(display_frame, "Zwarpowany obraz", (10, image.shape[0] + 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, text_color, 2)
    cv2.putText(display_frame, "Zwarpowany z wykrytymi ksztaltami", (image.shape[1] + 10, image.shape[0] + 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, text_color, 2)

    # Zapisz i wyświetl ramkę
    cv2.imwrite("all_views.jpg", display_frame)
    cv2.imshow("Analiza ksztaltow", display_frame)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

if __name__ == "__main__":
    main()