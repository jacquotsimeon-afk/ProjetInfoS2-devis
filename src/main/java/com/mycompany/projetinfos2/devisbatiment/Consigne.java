/* =========================================================================
 * NOM DE LA CLASSE : Consigne
 * DATE DE MISE À JOUR : Mai 2026
 * CATÉGORIE TECHNIQUE : Classe Utilitaire / Texte
 * =========================================================================
 * DESCRIPTION EN 2 LIGNES :
 * Contient tout le texte d'aide du logiciel. Permet de séparer les longs 
 * paragraphes d'explication du reste du code pour garder le projet propre.
 * =========================================================================
 * @author Clémentine
 * =========================================================================
 */
package com.mycompany.projetinfos2.devisbatiment;

public class Consigne {
    
    // Le mot-clé "static" permet d'utiliser cette méthode directement (Consigne.getTexte())
    // sans avoir besoin de créer un objet "new Consigne()" dans l'application.
    public static String getTexte() {
        return "--- MODALITÉS DE CRÉATION DU DEVIS ---\n\n"
             + "Il s'agit du devis intérieur d'une maison, on considère que tous les revêtements\n"
             + "concernent uniquement les murs intérieurs.\n\n"
             + "De même, il ne s'agit pas d'un plan représentatif factuel du bâtiment créé :\n"
             + "les fenêtres et portes sont placées de manière arbitraire, le but étant d'obtenir\n"
             + "le devis final et non pas le plan à bâtir.\n\n"
             + "1. CRÉATION DU PROJET\n"
             + "    - Donner le nom de votre projet\n"
             + "    - Choisir le Type (Maison de plain-pied : Maison\n"
             + "                       Maison à étage et immeuble : Immeuble)\n\n"
             + "2. MODE IMMEUBLE - Maison à ÉTAGES \n"
             + "   - Le niveau 0 (rez-de-chaussée) existe déjà.\n"
             + "   - La HSP est prédéfinie à 2.50m.\n"
             + "   - Ajouter un appartement pour commencer.\n"
             + "   - Ajouter une pièce puis la construire mur par mur en la dessinant à la souris.\n"
             + "     Cliquer une fois pour séléctionner la coordonnée initiale du mur, \n "
             + "     se déplacer et cliquer à nouveau pour bloquer la coordonnée finale \n"
             + "     (cliquer a nouveau pour modifier ou modifier dans la fenêtre de coordonnées).\n"
             + "   - Pour chaque mur les dimensions sont précisées dans la fenêtre accolée.\n"
             + "   - Ajouter un revêtement en sélectionnant le menu déroulant.\n"
             + "     Vous pouvez consulter le catalogue pour vous informer des prix au m².\n"
             + "   - Ajouter fenêtres et portes en indiquant le nombre.\n "
             + "     La légende restitue les portes et fenêtres ajoutées.\n "
             + "   - Cliquer sur terminer pièce pour ajouter un revêtement au sol/plafond.\n"
             + "     Possibilité de renommer la pièce, sinon Pièce (i = 1,...,n) par défaut.\n\n"
             + "Une fois le niveau 0 terminé, ajouter un niveau pour créer l'étage 1 et réitérer la procédure précédente.\n\n"
             + "Une fois l'immeuble terminé, (ou à n'importe quel moment du devis) cliquer sur 'Calculer le devis' "
             + "pour voir apparaître l'estimation totale.\n\n"
             + "Possibilité d'exporter le devis avec toutes les modalités précisées au cours de la construction.\n\n"
             + "3. MODE MAISON (PLAIN PIED)\n"
             + "   - Le niveau 0 (rez-de-chaussée) existe déjà.\n"
             + "   - La HSP est prédéfinie à 2.50m.\n"
             + "   - Ajouter une pièce pour commencer puis la construire mur par mur en la dessinant à la souris.\n"
             + "     Cliquer une fois pour séléctionner la coordonnée initiale du mur, \n "
             + "     se déplacer et cliquer à nouveau pour bloquer la coordonnée finale \n"
             + "     (cliquer a nouveau pour modifier ou modifier dans la fenêtre de coordonnées).\n"
             + "   - Pour chaque mur les dimensions sont précisées dans la fenêtre accolée.\n"
             + "   - Ajouter un revêtement en sélectionnant le menu déroulant.\n"
             + "     Vous pouvez consulter le catalogue pour vous informer des prix au m².\n"
             + "   - Ajouter fenêtres et portes en indiquant le nombre.\n "
             + "     La légende restitue les portes et fenêtres ajoutées.\n "
             + "   - Cliquer sur terminer pièce pour ajouter un revêtement au sol/plafond.\n"
             + "     Possibilité de renommer la pièce, sinon Pièce (i = 1,...,n) par défaut.\n\n"
             + "Une fois l'immeuble terminé, (ou à n'importe quel moment du devis) cliquer sur 'Calculer le devis' pour voir apparaître l'estimation totale.\n\n"
             + "Possibilité d'exporter le devis avec toutes les modalités précisées au cours de la construction.\n\n";
    }
}