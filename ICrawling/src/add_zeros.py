import os

def add_zeros(path, filename):
    new_dir = path + "/" + "with_zeros"
    new_file_path = new_dir + "/" + filename
    if not os.path.exists(new_dir):
        os.makedirs(new_dir)
    newfile = open(new_file_path, "a")
    with open(path + "/" + filename, "r") as f:
        for line in f.readlines():
            elements = line.split("\t")
            q = elements[0]
            d = elements[1]
            r = elements[2]
            new_elements = [q, "0", d, r]
            new_line = "\t".join(new_elements)
            newfile.write(new_line)
    newfile.close()

dirs = [d for d in os.listdir(os.getcwd()) if os.path.isdir(d)]
for d in dirs:
    add_zeros(d, "train.qrel")
    add_zeros(d, "dev.qrel")
    add_zeros(d, "test.qrel")
