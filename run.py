#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import sys

if len(sys.argv) < 2:
    print("./run.py [protocol]")
    sys.exit(0)

if not (sys.argv[1] == "MESI" or sys.argv[1] == "MSI"):
    print("Incorrect protocol")
    sys.exit(0)

if os.path.isfile("Simulator.jar"):
    os.remove("Simulator.jar")

os.system("jar cfe Simulator.jar Main -C ./bin/ .")


possible_nb_proc = [1, 2, 4, 8]
possible_benchs = ["FFT", "Weather"]
possible_cache_sizes = [1024, 2048, 4096, 16384, 32768]
possible_block_sizes = [8, 16, 32, 64, 128]
possible_associativity = [1, 2, 4]

def gen_dir_names():
    dirs_list = []
    for bench_name in possible_benchs:
        for i in possible_nb_proc:
            dirs_list.append(bench_name+"/"+bench_name+str(i))

    return dirs_list

dir_names = gen_dir_names()

for i in possible_nb_proc:
    for bench_name in possible_benchs:
            dir_name = bench_name+"/"+bench_name+str(i)
            os.system("java -jar Simulator.jar " + sys.argv[1] + " " + dir_name + " "+ str(i) +" " + "1024 1 16")

#os.system("java -jar Simulator.jar " + sys.argv[1] + " FFT/FFT2"+ " 2 " + " 1024 1 16")
