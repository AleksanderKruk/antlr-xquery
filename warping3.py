import cv2
import numpy as np
import math

def detect_and_warp_red_ring(image_path):
    # Wczytanie obrazu
    img = cv2.imread(image_path)
    if img is None:
        print(f"Nie można wczytać obrazu: {image_path}")
        return None

    # Stworzenie kopii do wizualizacji
    original = img.copy()

    # Konwersja do HSV (Hue, Saturation, Value) dla łatwiejszej detekcji kolorów
    hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)

    # Definicja zakresu koloru czerwonego w HSV
    # Czerwony kolor jest na początku i końcu zakresu Hue, więc potrzebujemy dwóch masek
    lower_red1 = np.array([0, 100, 100])
    upper_red1 = np.array([10, 255, 255])
    lower_red2 = np.array([160, 100, 100])
    upper_red2 = np.array([180, 255, 255])

    # Tworzenie masek dla czerwonego koloru
    mask1 = cv2.inRange(hsv, lower_red1, upper_red1)
    mask2 = cv2.inRange(hsv, lower_red2, upper_red2)
    mask = cv2.bitwise_or(mask1, mask2)

    # Usunięcie szumów za pomocą morfologii
    kernel = np.ones((5, 5), np.uint8)
    mask = cv2.morphologyEx(mask, cv2.MORPH_OPEN, kernel)
    mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, kernel)

    # Znajdowanie konturów
    contours, _ = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    if not contours:
        print("Nie znaleziono czerwonych obiektów")
        return None

    # Wybierz największy kontur (prawdopodobnie obręcz)
    largest_contour = max(contours, key=cv2.contourArea)

    # Znajdź otaczający prostokąt (bounding box)
    x, y, w, h = cv2.boundingRect(largest_contour)

    # Oblicz elipsę najlepiej pasującą do konturu
    ellipse = cv2.fitEllipse(largest_contour)
    center, axes, angle = ellipse

    # Narysuj wykryty kontur i prostokąt na oryginalnym obrazie (do wizualizacji)
    img_contours = original.copy()
    cv2.drawContours(img_contours, [largest_contour], -1, (0, 255, 0), 2)
    cv2.rectangle(img_contours, (x, y), (x+w, y+h), (255, 0, 0), 2)
    cv2.ellipse(img_contours, ellipse, (0, 0, 255), 2)

    # Zapisz obraz z wykrytym konturem
    cv2.imwrite("detected_contour.jpg", img_contours)

    # Oblicz stosunek szerokości do wysokości (aspect ratio)
    aspect_ratio = w / h

    # Oblicz docelowy rozmiar
    # Wybieramy największy wymiar jako średnicę koła
    target_diameter = max(w, h)

    # Obliczamy macierz transformacji
    # Jeśli aspect_ratio != 1, potrzebujemy przeprowadzić warping, aby uzyskać koło
    src_points = np.float32([
        [x, y],                  # Lewy górny
        [x + w, y],              # Prawy górny
        [x + w, y + h],          # Prawy dolny
        [x, y + h]               # Lewy dolny
    ])

    # Jeśli stosunek szerokości do wysokości jest większy od 1, to obręcz jest spłaszczona poziomo
    # W przeciwnym przypadku jest spłaszczona pionowo
    if aspect_ratio > 1:
        # Spłaszczenie poziome - rozciągamy pionowo
        new_h = w  # Nowa wysokość równa szerokości
        offset_y = (new_h - h) / 2
        dst_points = np.float32([
            [x, y - offset_y],
            [x + w, y - offset_y],
            [x + w, y + h + offset_y],
            [x, y + h + offset_y]
        ])
    else:
        # Spłaszczenie pionowe - rozciągamy poziomo
        new_w = h  # Nowa szerokość równa wysokości
        offset_x = (new_w - w) / 2
        dst_points = np.float32([
            [x - offset_x, y],
            [x + w + offset_x, y],
            [x + w + offset_x, y + h],
            [x - offset_x, y + h]
        ])

    # Oblicz macierz transformacji perspektywicznej
    M = cv2.getPerspectiveTransform(src_points, dst_points)

    # Oblicz docelowy rozmiar obrazu po transformacji
    if aspect_ratio > 1:
        warped_size = (img.shape[1], int(img.shape[0] * (1 + (aspect_ratio - 1))))
    else:
        warped_size = (int(img.shape[1] * (1 + (1/aspect_ratio - 1))), img.shape[0])

    # Przeprowadź transformację
    warped = cv2.warpPerspective(img, M, (warped_size[0], warped_size[1]))

    # Alternatywne podejście - użyj transformacji afinicznej
    # To często daje lepsze wyniki niż perspektywiczna dla prostych deformacji
    # Znajdź środek i osie elipsy
    center_x, center_y = center
    major_axis, minor_axis = axes

    # Oblicz macierz transformacji afinicznej na podstawie elipsy
    # Przekształcamy elipsę na koło
    scale_x = major_axis / minor_axis if major_axis > minor_axis else 1.0
    scale_y = minor_axis / major_axis if minor_axis < major_axis else 1.0

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
    target_center_x = img.shape[1] // 2
    target_center_y = img.shape[0] // 2

    affine_mat[0, 2] = target_center_x - center_x
    affine_mat[1, 2] = target_center_y - center_y

    # Wykonaj transformację afiniczną
    affine_warped = cv2.warpAffine(img, affine_mat, (img.shape[1], img.shape[0]))

    # Zwróć oba wyniki do porównania
    return {
        "perspective_warped": warped,
        "affine_warped": affine_warped,
        "detected_contour": img_contours
    }

def main():
    # Ścieżka do obrazu
    image_path = r"C:\Users\aleks\Desktop\30znak.png"  # Zmień na swoją ścieżkę do obrazu

    # Wywołanie funkcji detekcji i warpingu
    result = detect_and_warp_red_ring(image_path)

    if result:
        # Zapisz wyniki
        cv2.imwrite("perspective_warped.jpg", result["perspective_warped"])
        cv2.imwrite("affine_warped.jpg", result["affine_warped"])

        # Pokaż wyniki
        cv2.imshow("Original with Detection", result["detected_contour"])
        cv2.imshow("Perspective Warped", result["perspective_warped"])
        cv2.imshow("Affine Warped", result["affine_warped"])
        cv2.waitKey(0)
        cv2.destroyAllWindows()

if __name__ == "__main__":
    main()