# Markdown to Pandoc to ConTeXt to PDF

## Présentation

L'idée de ce projet est de faciliter la rédaction de rapport de cours en
utilisant Markdown pour le contenu du rapport, `pandoc` pour la conversion en
xml et enfin `context` pour lire du contenu du fichier xml et le convertir en
pdf avec tout un tas de règles de transformation.

Cet outils est encore en cours de développement et répond surtout à des besoins
en particulier.

Je le mets sur ce repo en attendant d'en faire un plus vrai. (cimer Alex)

## Pré-requis

Voici ce qu'il faut avoir pour utiliser cet outils:

- `pandoc` avec très probablement `pandoc-plantuml` comme filtre,
- `context`,
- `inkscape` pour l'utilisation de fichiers svg directement dans ConTeXt,
- pandoc-2-context, quelqu'un qui a déjà fait une très grande partie de la
  conversion d'un xml généré par pandoc qu'on peut personnalisé au besoin.

Pour le moment.

## Utilisation

Modifiez les fichier setup.tex et title.tex selon vos besoins. Choisissez des
noms de fichiers pertinents et faire les changements nécessaire dans le
Makefile.

Une fois que votre rapport est prêt pour être compilé, simplement taper `make`
dans le terminal et voir la "magie" s'opérer <sub>(en vrai, c'est vraiment trop
stylé comme on arrive avec des trucs sympa)</sub>.

Voilà voilà, amusez-vous bien.

Fonctionne très probablement sur Windows, jamais testé personnellement
cependant…

## Remarque

Comme c'est surtout pour répondre à un besoin des cours pour le moment, le
modèle est quelque peu adapté pour l'HEIG-VD, mais rien ne vous empêche de
faire les changements pour l'adapter à votre situation en attendant de faire
mieux.
