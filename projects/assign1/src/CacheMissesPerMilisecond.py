import numpy as np
from matplotlib import pyplot as plt

cpp_data = open("c++/c++_lineBigger_product_data.txt").readlines()


cpp_data = [x.split(";") for x in cpp_data]




cpp_data = [[int(x[0]), float(x[1]),float(x[2])] for x in cpp_data]

cpp_xarr = np.array([x[0] for x in cpp_data])
cpp_yarr = np.array([x[2]/(x[1] * 1000) for x in cpp_data])




for index in range(len(cpp_xarr)):
    plt.plot([cpp_xarr[index], cpp_xarr[index]], color="k")
plt.plot(cpp_xarr, cpp_yarr, "r", marker="D", label="C++")

plt.legend(loc="upper left", frameon=False)
#plt.xlim(xmin=400, xmax=3500)
plt.xlim(xmin=3500, xmax=11000)
plt.grid()


for x_item, y_item in np.nditer([cpp_xarr, cpp_yarr]):
    label = "{:.2f}".format(y_item) + "/ms"
    plt.annotate(label, (x_item, y_item), textcoords="offset points", xytext=(20,-15),ha="center")

plt.xlabel("Matrix Size (n in a nxn matrix)")
plt.ylabel("L1 cache misses per milissecond (miss/ms)")
plt.show()
