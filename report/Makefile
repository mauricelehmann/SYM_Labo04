all: Rapport-SYM-labo4.pdf

%.pdf: %.tex %.xml setup.tex title/title.tex
	PATH="./tools:$$PATH" context --purgeall --environment="$<" "$(@:pdf=xml)"

Rapport-SYM-labo4.xml: 10-report.md
	mkdir -p vimoutput
	pandoc --no-highlight -s --section-divs --filter pandoc-plantuml -o "$@" -t html $^
