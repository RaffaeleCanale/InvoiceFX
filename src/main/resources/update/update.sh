sleep 10

jarFile="$1/InvoiceFX.jar"
updatedJarFile="$2/InvoiceFX.jar"

mv "$updatedJarFile" "$jarFile"

java -jar "$jarFile"
