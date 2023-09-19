import numpy as np
from matplotlib import pyplot as plt

cpp_data = open("c++/c++_dot_product_data.txt").readlines()
java_data = open("java/java_dot_product_data.txt").readlines()

cpp_data = [x.split(";") for x in cpp_data]
java_data = [x.split(";") for x in java_data]



cpp_data = [[int(x[0]), float(x[1])] for x in cpp_data]
java_data = [[int(x[0]), float(x[1])] for x in java_data]

cpp_xarr = np.array([x[0] for x in cpp_data])
cpp_yarr = np.array([x[1] for x in cpp_data])

java_xarr = np.array([x[0] for x in java_data])
java_yarr = np.array([x[1] for x in java_data])


for index in range(len(cpp_xarr)):
    plt.plot([cpp_xarr[index], cpp_xarr[index]], [java_yarr[index], java_yarr[index]], color="k")
plt.plot(java_xarr, java_yarr, "g", marker="D", label="Java")
plt.plot(cpp_xarr, cpp_yarr, "r", marker="D", label="C++")

plt.legend(loc="upper left", frameon=False)
plt.xlim(xmin=400, xmax=3200)
plt.grid()

for x_item, y_item in np.nditer([java_xarr, java_yarr]):
    label = "{:.2f}".format(y_item) + "s"
    plt.annotate(label, (x_item, y_item), textcoords="offset points", xytext=(-25,10),ha="center")
for x_item, y_item in np.nditer([cpp_xarr, cpp_yarr]):
    label = "{:.2f}".format(y_item) + "s"
    plt.annotate(label, (x_item, y_item), textcoords="offset points", xytext=(20,-15),ha="center")

plt.xlabel("Matrix Size (n in a nxn matrix)")
plt.ylabel("Time of execution (seconds)")
plt.show()
