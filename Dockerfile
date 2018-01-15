FROM openjdk:8
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
CMD ["/usr/bin/java","-jar","dist/pdfmark.jar","-d","10.5555/12345678","test-data/test-pdf.pdf","-o","/out"]