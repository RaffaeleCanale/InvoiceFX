#!/bin/bash

mvn package

cp "target/invoicefx-0.1-SNAPSHOT-jar-with-dependencies.jar" "deploy/InvoiceFX.jar"

cd deploy
java -jar "InvoiceFX.jar" --create-index
java -jar "InvoiceFX.jar" --add-url "InvoiceFX.jar" "https://drive.google.com/uc?export=download&id=0B6LgrYnciPdhRk5mbjhUZTFWZlE"
jsync sync
