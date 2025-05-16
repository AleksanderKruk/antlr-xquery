import cv2
import numpy as np

# 1. Wczytaj i przekształć na skalę szarości
image = cv2.imread(r"C:\Users\aleks\Desktop\3skewed.png")
gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

# 2. Wygładź i wykryj krawędzie
blurred = cv2.GaussianBlur(gray, (5, 5), 0)
edges = cv2.Canny(blurred, 50, 150)

# 3. Znajdź kontury
contours, _ = cv2.findContours(edges.copy(), cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

# 4. Wybierz największy kontur
contour = max(contours, key=cv2.contourArea)

# 5. Przybliż go do 4-punktowego kształtu (jeśli możliwe)
epsilon = 0.02 * cv2.arcLength(contour, True)
approx = cv2.approxPolyDP(contour, epsilon, True)
for point in approx:
    cv2.circle(image, point[0], 5, (0, 0, 255), -1)
cv2.imshow("Kontur", image)
cv2.waitKey(-1)

if len(approx) == 4:
    # Mamy 4 punkty – możliwy warp!
    pts_src = np.array([point[0] for point in approx], dtype="float32")

    # Uporządkuj punkty w kolejności TL, TR, BR, BL
    def order_points(pts):
        rect = np.zeros((4, 2), dtype="float32")
        s = pts.sum(axis=1)
        diff = np.diff(pts, axis=1)
        rect[0] = pts[np.argmin(s)]  # TL
        rect[2] = pts[np.argmax(s)]  # BR
        rect[1] = pts[np.argmin(diff)]  # TR
        rect[3] = pts[np.argmax(diff)]  # BL
        return rect

    rect = order_points(pts_src)

    # Oblicz wymiary prostokąta docelowego
    (tl, tr, br, bl) = rect
    width = int(max(np.linalg.norm(tr - tl), np.linalg.norm(br - bl)))
    height = int(max(np.linalg.norm(bl - tl), np.linalg.norm(br - tr)))

    # Docelowe punkty prostokąta
    dst = np.array([
        [0, 0],
        [width - 1, 0],
        [width - 1, height - 1],
        [0, height - 1]
    ], dtype="float32")

    # 6. Transformacja perspektywiczna (dewarp)
    M = cv2.getPerspectiveTransform(rect, dst)
    warped = cv2.warpPerspective(image, M, (width, height))

    cv2.imwrite("dewarped.jpg", warped)
else:
    print("Nie znaleziono dokładnie 4 punktów – nie można zdewarpować.")
