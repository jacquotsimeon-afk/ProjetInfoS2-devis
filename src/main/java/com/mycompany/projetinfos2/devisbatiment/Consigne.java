package com.mycompany.projetinfos2.devisbatiment;

public class Consigne {
    
    // Méthode simple qui renvoie tout le texte des consignes
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
             + "2. MODE IMMEUBLE\n"
             + "   - Le niveau 0 (rez-de-chaussée) existe déjà.\n"
             + "   - La HSP est prédéfinie à 2.50m.\n"
             + "   - Ajouter un appartement pour commencer.\n"
             + "   - Ajouter une pièce puis la construire mur par mur en la dessinant à la souris.\n"
             + "   - Pour chaque mur les dimensions sont précisées dans la fenêtre accolée.\n"
             + "   - Ajouter un revêtement en sélectionnant le menu déroulant.\n"
             + "     Vous pouvez consulter le catalogue pour vous informer des prix au m².\n"
             + "   - Ajouter fenêtres et portes en indiquant le nombre.\n"
             + "   - Cliquer sur terminer pièce pour ajouter un revêtement au sol/plafond.\n"
             + "     Possibilité de renommer la pièce, sinon Pièce (i) par défaut.\n\n"
             + "Une fois le niveau 0 terminé, ajouter un niveau pour créer l'étage 1 et réitérer la procédure précédente.\n\n"
             + "Une fois l'immeuble terminé, cliquer sur 'Calculer le devis' pour voir apparaître l'estimation totale.\n\n"
             + "Possibilité d'exporter le devis avec toutes les modalités précisées au cours de la construction.";
    }
}