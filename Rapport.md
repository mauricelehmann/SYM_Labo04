# Questions

# Capteurs
## Question
> Une fois la manipulation effectuée, vous constaterez que les animations de la flèche ne sont pas fluides, il va y avoir un tremblement plus ou moins important même si le téléphone ne bouge pas. Veuillez expliquer quelle est la cause la plus probable de ce tremblement et donner une manière (sans forcément l’implémenter) d’y remédier.

Réponse : 
Nous pensons que cela peut provenir des points suivants:
- La précision des capteurs n'est pas parfaite. En effet même lorsque le smartphone est immobile, les valeurs continuent de changer. Cela peut venir des capteurs ou alors on peut avoir des interférences en fonction de l'endroit ou l'on se trouve.
- Lorsque il y a un changement conséquent la valeur change de manière conséquente. Cela donne une impréssion de "saccadé", car la valeur "saute" à la suivante suite au changement.

Pour remédier à cela il s'agit de faire en sorte que changements soient aplatis et que cela offre une sensation de fluidité. la solution que nous proposons est d'utiliser la moyenne mobile pour afficher la valeur à l'écran. (La moyenne mobile est la moyenne des N dernières valeurs.)


# Communication Bluetooth Low Energy
## Questions
> La caractéristique permettant de lire la température retourne la valeur en degrés Celsius,multipliée par 10,sous la forme d’un entier non-signé de 16bits. Quel est l’intérêt de procéder de la sorte? Pourquoi ne pas échanger un nombre à virgule flottante de type float par exemple?

Réponse : 
Généralement un float est sur 32bits. Ainsi en utilisant le système de l'entier non signé, cela prend 2 fois moins de place. Nous pensons donc que c'est pour réduite / économiser la quantité de données échangées.


> Le niveau de charge de la pile est à présent indiqué uniquement sur l’écran du périphérique, mais nous souhaiterions que celui-ci puisse informer le smartphone sur son niveau de charge restante.Veuillez spécifier la(les) caractéristique(s) qui composerai(en)t un tel service,mis à disposition par le périphérique et permettant de communiquer le niveau de batterie restant via Bluetooth Low Energy.Pour chaque caractéristique, vous indiquerez les opérations supportées (lecture, écriture, notification, indication, etc.) ainsi que les données échangées et leur format.

Réponse : 
