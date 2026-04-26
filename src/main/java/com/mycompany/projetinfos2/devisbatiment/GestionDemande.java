/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.projetinfos2.devisbatiment;

/**
 *
 * @author Siméon
 */
public class GestionDemande {
    private Revetement[] catalogue;
    private int nbRev;

    public GestionDemande(Revetement[] c, int n) {
        this.catalogue = c;
        this.nbRev = n;
    }

    public Batiment creerBatiment() {
        System.out.println("\n--- Creation d'un nouveau projet ---");
        System.out.print("Type (1: Maison, 2: Immeuble) : ");
        int type = Lire.choixBinaire();
        System.out.print("Nom/ID du batiment : ");
        String id = Lire.S();

        if (type == 1) {
            System.out.print("Nombre de pieces (max 500) : ");
            int nbP = Lire.entierCompris(1, 500);
            Maison m = new Maison(id, nbP);
            for (int i = 0; i < nbP; i++) {
                m.ajouterPiece(saisirPiece(i + 1, 2.5)); 
            }
            return m;
        } else {
            System.out.print("Nombre de niveaux (max 500) : ");
            int nbN = Lire.entierCompris(1, 500);
            Immeuble imm = new Immeuble(id, nbN);
            for (int i = 0; i < nbN; i++) {
                imm.ajouterNiveau(saisirNiveau(i));
            }
            return imm;
        }
    }

    private Niveau saisirNiveau(int idN) {
        System.out.print("Hauteur sous plafond (m) pour le niveau " + idN + " : ");
        double h = Lire.d();
        System.out.print("Nombre d'appartements : ");
        int nbA = Lire.entierCompris(1, 100);
        Niveau niv = new Niveau(idN, h, nbA);
        for (int i = 0; i < nbA; i++) {
            System.out.print("Nombre de pieces pour l'appart " + (i + 1) + " : ");
            int nbP = Lire.entierCompris(1, 50);
            Appartement app = new Appartement(i + 1, nbP);
            for (int j = 0; j < nbP; j++) {
                app.ajouterPiece(saisirPiece(j + 1, h));
            }
            niv.ajouterAppart(app);
        }
        return niv;
    }

    private Piece saisirPiece(int idP, double h) {
        while (true) { // Boucle pour recommencer la piece
            System.out.println("\n--- Configuration de la Piece numero " + idP + " ---");
            System.out.print("Nombre de murs (ex: 4 pour rectangle) : ");
            int nbM = Lire.entierCompris(3, 50);
            Piece p = new Piece(idP, nbM);
            boolean piecePossedeOuverture = false;

            for (int i = 0; i < nbM; i++) {
                System.out.println("  Mur " + (i + 1));
                System.out.print("    X debut (m): "); double x1 = Lire.d();
                System.out.print("    Y debut (m): "); double y1 = Lire.d();
                System.out.print("    X fin (m): "); double x2 = Lire.d();
                System.out.print("    Y fin (m): "); double y2 = Lire.d();
                Mur m = new Mur(i + 1, new Coin(1, x1, y1), new Coin(2, x2, y2), 10);
                
                int nbO;
                // Verifie si c'est le dernier mur et s'il n'y a toujours pas d'ouverture
                while (true) {
                    System.out.print("    Nombre d'ouvertures pour ce mur : ");
                    nbO = Lire.entierCompris(0, 10);
                    
                    if (i == nbM - 1 && !piecePossedeOuverture && nbO == 0) {
                        System.out.println("    [ERREUR] Une piece doit forcement posseder au moins une ouverture !");
                        System.out.println("    Veuillez en ajouter au moins une sur ce dernier mur.");
                    } else {
                        break; // Saisie correcte
                    }
                }
                
                if (nbO > 0) piecePossedeOuverture = true;

                for (int k = 0; k < nbO; k++) {
                    System.out.print("    Type ouverture " + (k + 1) + " (1: Porte, 2: Fenetre) : ");
                    if (Lire.choixBinaire() == 1) m.ajouterOuverture(new Porte(k + 1));
                    else m.ajouterOuverture(new Fenetre(k + 1));
                }
                appliquerRev(m, "Mur");
                p.ajouterMur(m);
            }
            appliquerRev(p.getSol(), "Sol");
            appliquerRev(p.getPlafond(), "Plafond");

            System.out.print("\nConfiguration de la piece terminee. Voulez-vous la recommencer ? (1: Oui, 2: Non) : ");
            if (Lire.choixBinaire() == 2) {
                return p; // On valide la piece et on sort
            }
            System.out.println("-> On recommence la configuration de la piece " + idP + ".\n");
        }
    }

    private void appliquerRev(Object obj, String type) {
        System.out.print("  Appliquer un revetement sur " + type + " ? (1: Oui, 2: Non) : ");
        if (Lire.choixBinaire() == 1) {
            
            // Affichage automatique du catalogue filtre pour plus de clarte
            System.out.println("  --- Revetements disponibles pour : " + type + " ---");
            for (int i = 0; i < nbRev; i++) {
                boolean compatible = false;
                if (type.equals("Mur") && catalogue[i].estPourMur()) compatible = true;
                if (type.equals("Sol") && catalogue[i].estPourSol()) compatible = true;
                if (type.equals("Plafond") && catalogue[i].estPourPlafond()) compatible = true;
                
                if (compatible) {
                    System.out.println("   ID " + catalogue[i].getId() + " : " + catalogue[i].getDesignation() + " (" + catalogue[i].getPrix() + " EUR/m2)");
                }
            }

            boolean ok = false;
            while (!ok) {
                System.out.print("  Saisissez l'ID du materiau choisi dans la liste ci-dessus : ");
                int id = Lire.i();
                
                for (int i = 0; i < nbRev; i++) {
                    if (catalogue[i].getId() == id) {
                        if ((type.equals("Mur") && catalogue[i].estPourMur()) ||
                            (type.equals("Sol") && catalogue[i].estPourSol()) ||
                            (type.equals("Plafond") && catalogue[i].estPourPlafond())) {
                            
                            if (obj instanceof Mur) ((Mur)obj).appliquerRevetement(catalogue[i]);
                            else if (obj instanceof Sol) ((Sol)obj).appliquerRevetement(catalogue[i]);
                            else ((Plafond)obj).appliquerRevetement(catalogue[i]);
                            ok = true;
                        } else {
                            System.out.println("  [ERREUR] Ce materiau n'est pas autorise pour un " + type + " !");
                        }
                    }
                }
            }
        }
    }
}