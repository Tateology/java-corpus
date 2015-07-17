# find . -name "build.xml" | xargs sed -i 's/<javac/<javac fork="true" executable="${mycompiler}"/g' # script to swap out the compiler
ant -Dmycompiler=javac clean build-nozip
