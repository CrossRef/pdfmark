Testing
=====================================================

Extended test data (in the form of lots of PDFs)
can be inserted as a git submodule. Use these
instructions to get the test data. First check out
the pdftestadata repo from labs.crossref.org:

$ git clone <your_user_name>@labs.crossref.org:
        /home/kward/pdftestdata

Then initialise
git submodules within your pdfmark repo:

$ git submodule init

Then alter your ~/.git/config file - point the
"test-data/extended" submodule to your local
copy of the pdftestdata repo. Finally, update the
submodules within your pdfmark repo:

$ git submodule update

