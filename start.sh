#!/bin/bash
javac -encoding utf-8 Trajic.java Codebook.java CSVReader.java DynamicEncoder.java Encoder.java GPSPoint.java GPSReader.java Huffman.java LengthFrequencyDivider.java LinearPredictor.java NaiveLinearPredictor.java Node.java PredictiveCompressor.java Predictor.java PublicFunc.java TXTReader.java -d .
java trajic.Trajic c E:\Docu\Science\sigmod\code\tem\000001.csv 0 0