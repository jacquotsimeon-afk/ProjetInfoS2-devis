/* =========================================================================
 * NOM DE LA CLASSE : GestionDemande
 * DATE DE MISE À JOUR : Mai 2026
 * CATÉGORIE TECHNIQUE : Interface Console / Saisie texte (Ancienne version)
 * =========================================================================
 * DESCRIPTION EN 2 LIGNES :
 * Gère les interactions en mode texte avec l'utilisateur dans la console.
 * Pose les questions (murs, ouvertures, revêtements) pour construire l'arbre du projet.
 * =========================================================================
 * @author Clémentine / Siméon
 * =========================================================================
 */
package com.mycompany.projetinfos2.devisbatiment;

public class GestionDemande {
    // Référence vers le catalogue de matériaux pour valider les choix de saisie
    private Revetement[] catalogue;
    private int nbRev;

    // Constructeur : associe le catalogue de prix à notre gestionnaire de saisie
    public GestionDemande(Revetement[] c, int n) { 
        this.catalogue = c; 
        this.nbRev = n; 
    }

    // Lance l'interrogatoire général dans la console pour instancier le projet
    public Batiment creerBatiment() {
        System.out.println("\n--- Creation d'un nouveau projet ---");
        System.out.print("Type (1: Maison, 2: Immeuble) : ");
        int type = Lire.choixBinaire();
        
        System.out.print("Nom/ID du batiment : ");
        String id = Lire.S();

        if (type == 1) { // Mode Maison de plain-pied
            System.out.print("Nombre de pieces : ");
            int nbP = Lire.entierCompris(1, 500);
            Maison m = new Maison(id, nbP);
            // Boucle itérative pour forcer le paramétrage séquentiel de chaque pièce
            for (int i = 0; i < nbP; i++) m.ajouterPiece(saisirPiece(i + 1, 2.5));
            return m;
        } else { // Mode Immeuble à étages
            System.out.print("Nombre de niveaux : ");
            int nbN = Lire.entierCompris(1, 500);
            Immeuble imm = new Immeuble(id, nbN);
            for (int i = 0; i < nbN; i++) imm.ajouterNiveau(saisirNiveau(i));
            return imm;
        }
    }

    // Gère la configuration pas à pas d'un étage (Niveau)
    private Niveau saisirNiveau(int id) {
        System.out.println("\n  --- Configuration du Niveau " + id + " ---");
        System.out.print("  Hauteur sous plafond (m) [defaut 2.5] : ");
        double h = Lire.d();
        if (h <= 0) h = 2.5; // Sécurité de format

        System.out.print("  Nombre d'appartements à ce niveau : ");
        int nbA = Lire.entierCompris(1, 100);
        Niveau niv = new Niveau(id, h, nbA);

        for (int i = 0; i < nbA; i++) {
            System.out.println("\n    > Appartement " + (i + 1) + " / " + nbA);
            System.out.print("    Nombre de pieces pour cet appart : ");
            int nbP = Lire.entierCompris(1, 100);
            Appartement app = new Appartement(i + 1, nbP);
            
            for (int j = 0; j < nbP; j++) {
                app.ajouterPiece(saisirPiece(j + 1, h));
            }
            niv.ajouterAppart(app);
        }
        return niv;
    }

    // Configure individuellement une pièce géométrique (Murs, Portes, Sol...)
    private Piece saisirPiece(int id, double h) {
        while (true) { // Boucle de sécurité : permet de recommencer la pièce en cas de fausse saisie
            System.out.println("\n    --- Saisie de la Piece n°" + id + " ---");
            System.out.print("    Nombre de murs (min 3) : ");
            int nbM = Lire.entierCompris(3, 100);
            Piece p = new Piece(id, nbM);

            // Saisie géométrique des coins pour chaque mur de la pièce
            for (int i = 0; i < nbM; i++) {
                System.out.println("      * Mur n°" + (i + 1) + " / " + nbM);
                System.out.print("        Coin Debut X, Y (metres) : ");
                Coin cDeb = new Coin(1, Lire.d(), Lire.d());
                System.out.print("        Coin Fin X, Y (metres) : ");
                Coin cFin = new Coin(2, Lire.d(), Lire.d());

                System.out.print("        Nombre d'ouvertures (Portes/Fenêtres) : ");
                int nbO = Lire.entierCompris(0, 20);
                Mur m = new Mur(i + 1, cDeb, cFin, nbO);

                // Ajout des ouvertures portées par la cloison
                for (int j = 0; j < nbO; j++) {
                    System.out.print("        Type ouverture (1: Porte, 2: Fenetre) : ");
                    if (Lire.choixBinaire() == 1) m.ajouterOuverture(new Porte(j + 1));
                    else m.ajouterOuverture(new Fenetre(j + 1));
                }
                
                // Choix optionnel du matériau de finition pour le mur actuel
                appliquerRev(m, "Mur");
                p.ajouterMur(m);
            }

            // Saisie optionnelle des trémies (vides de plancher) dans le sol de la pièce
            System.out.print("    Nombre de tremies au sol (0 si aucun) : ");
            int nbT = Lire.entierCompris(0, 10);
            for (int k = 0; k < nbT; k++) {
                System.out.print("      Surface de la tremie n°" + (k + 1) + " (m2) : ");
                p.getSol().ajouterTremie(new Tremie(k + 1, Lire.d()));
            }
            
            // Paramétrage des finitions pour les deux grandes surfaces horizontales
            appliquerRev(p.getSol(), "Sol");
            appliquerRev(p.getPlafond(), "Plafond");

            System.out.print("    Recommencer la saisie de cette piece ? (1:Oui, 2:Non) : ");
            if (Lire.choixBinaire() == 2) return p; // On ne sort et retourne la pièce que si l'utilisateur valide
        }
    }

    // Méthode générique filtrant le catalogue selon le type de surface visée
    private void appliquerRev(Object obj, String type) {
        System.out.print("  Appliquer un revetement sur [" + type + "] ? (1:Oui, 2:Non) : ");
        if (Lire.choixBinaire() == 1) {
            System.out.println("  --- Catalogue materiaux pour : " + type + " ---");
            for (int i = 0; i < nbRev; i++) {
                // Filtrage polymorphe basé sur les droits booléens de chaque article
                boolean comp = (type.equals("Mur") && catalogue[i].estPourMur()) || 
                               (type.equals("Sol") && catalogue[i].estPourSol()) || 
                               (type.equals("Plafond") && catalogue[i].estPourPlafond());
                if (comp) {
                    System.out.println("   ID " + catalogue[i].getId() + " : " + catalogue[i].getDesignation() + " (" + catalogue[i].getPrix() + " €/m2)");
                }
            }
            System.out.print("  ID de l'article choisi : ");
            int id = Lire.i();
            
            // Association de l'élément sélectionné sur l'objet cible reçu en paramètre
            for (int i = 0; i < nbRev; i++) {
                if (catalogue[i].getId() == id) {
                    if (obj instanceof Mur) ((Mur) obj).appliquerRevetement(catalogue[i]);
                    else if (obj instanceof Sol) ((Sol) obj).appliquerRevetement(catalogue[i]);
                    else if (obj instanceof Plafond) ((Plafond) obj).appliquerRevetement(catalogue[i]);
                    break;
                }
            }
        }
    }
}