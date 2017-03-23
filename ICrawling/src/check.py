d = 0
with open("filtered_nfdump.txt", "r") as f:
    for line in f.readlines():
        descr = line.split("\t")[4]
        if descr != "-":
            d += 1;
print("DESCRIPTIONS: ", d)
