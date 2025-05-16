import pprint
import cv2
import numpy as np

from warping3 import detect_and_warp_red_ring
def warp(image):
    # Wyznacz najmniejszy prostokąt otaczający kontur
    contours, _ = cv2.findContours(image, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    # Załóżmy, że masz tylko jeden obiekt – bierzemy największy kontur
    cnt = max(contours, key=cv2.contourArea)
    rect = cv2.minAreaRect(cnt)


    # Pobierz cztery narożniki tego prostokąta
    box = cv2.boxPoints(rect)  # Zwraca punkty float32
    pts_src = np.array(box, dtype="float32")
    # Docelowy prostokątny rozmiar (np. 200x300)
    width, height = 100, 200
    pts_dst = np.array([
        [0, 0],
        [width - 1, 0],
        [width - 1, height - 1],
        [0, height - 1]
    ], dtype="float32")

    # Wyznaczenie macierzy transformacji perspektywicznej
    M = cv2.getPerspectiveTransform(pts_src, pts_dst)

    # Przekształcenie perspektywy
    warped = cv2.warpPerspective(image, M, (width, height))
    return warped

def getMoments(p, wapring=False):
    image = cv2.imread(p, cv2.IMREAD_GRAYSCALE)
    result = cv2.threshold(image, 127, 255, cv2.THRESH_BINARY_INV)
    resized = warp(result[1]) if wapring else cv2.resize(result[1], (100, 200), interpolation=cv2.INTER_NEAREST)
    moments = cv2.moments(resized)
    humoments = cv2.HuMoments(moments)
    pprint.pprint(humoments)
    logged = np.abs(humoments)
    logged = np.log(logged)
    pprint.pprint(logged)
    return humoments, logged, resized




def distance(a, b):
    return np.sqrt(np.sum((a - b) ** 2))


# humoments3, hulogged3, im3 = getMoments(r"C:\Users\aleks\Desktop\3.png")
# humoments0, hulogged0, im0 = getMoments(r"C:\Users\aleks\Desktop\0.png")
# humoments3skewed, hulogged3skewed, im3skewed = getMoments(r"C:\Users\aleks\Desktop\3skewed.png", True)
# humoments0skewed, hulogged0skewed, im0skewed = getMoments(r"C:\Users\aleks\Desktop\0skewed.png", True)
# cv2.imshow("Thresholded Image 3", im3)
# cv2.imshow("Thresholded Image 0", im0)
# cv2.imshow("Thresholded Image 3 skewed", im3skewed)
# cv2.imshow("Thresholded Image 0 skewed", im0skewed)

# # print("Distance between 3 and 0: ", distance(hulogged3, hulogged0))
# print("Distance between 3 and 3 skewed: ", distance(hulogged3, hulogged3skewed))
# print("Distance between 0 and 3 skewed: ", distance(hulogged0, hulogged3skewed))
# print("Distance between 3 and 0 skewed: ", distance(hulogged3, hulogged0skewed))
# print("Distance between 0 and 0 skewed: ", distance(hulogged0, hulogged0skewed))
