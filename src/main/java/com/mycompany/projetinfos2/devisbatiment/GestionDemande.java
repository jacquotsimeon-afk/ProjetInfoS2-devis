package com.mycompany.projetinfos2.devisbatiment;

public class GestionDemande {
    private Revetement[] catalogue;
    private int nbRev;

    public GestionDemande(Revetement[] c, int n) { this.catalogue = c; this.nbRev = n; }

    public Batiment creerBatiment() {
        System.out.println("\n--- Creation d'un nouveau projet ---");
        System.out.print("Type (1: Maison, 2: Immeuble) : ");
        int type = Lire.choixBinaire();
        System.out.print("Nom/ID du batiment : ");
        String id = Lire.S();

        if (type == 1) {
            System.out.print("Nombre de pieces : ");
            int nbP = Lire.entierCompris(1, 500);
            Maison m = new Maison(id, nbP);
            for (int i = 0; i < nbP; i++) m.ajouterPiece(saisirPiece(i + 1, 2.5));
            return m;
        } else {
            System.out.print("Nombre de niveaux : ");
            int nbN = Lire.entierCompris(1, 500);
            Immeuble imm = new Immeuble(id, nbN);
            for (int i = 0; i < nbN; i++) imm.ajouterNiveau(saisirNiveau(i));
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
            System.out.print("Nb pieces appart " + (i + 1) + " : ");
            int nbP = Lire.entierCompris(1, 50);
            Appartement app = new Appartement(i + 1, nbP);
            for (int j = 0; j < nbP; j++) app.ajouterPiece(saisirPiece(j + 1, h));
            niv.ajouterAppart(app);
        }
        return niv;
    }

    private Piece saisirPiece(int idP, double h) {
        while (true) {
            System.out.println("\n--- Config Piece " + idP + " ---");
            System.out.print("Nombre de murs : ");
            int nbM = Lire.entierCompris(3, 50);
            Piece p = new Piece(idP, nbM);
            boolean aOuv = false;

            for (int i = 0; i < nbM; i++) {
                System.out.println("  Mur " + (i + 1));
                System.out.print("    X1: "); double x1 = Lire.d(); System.out.print("    Y1: "); double y1 = Lire.d();
                System.out.print("    X2: "); double x2 = Lire.d(); System.out.print("    Y2: "); double y2 = Lire.d();
                Mur m = new Mur(i + 1, new Coin(1, x1, y1), new Coin(2, x2, y2), 10);
                
                int nbO;
                while (true) {
                    System.out.print("    Nb ouvertures : "); nbO = Lire.entierCompris(0, 10);
                    if (i == nbM - 1 && !aOuv && nbO == 0) System.out.println("    [ERREUR] Il faut une ouverture !");
                    else break;
                }
                if (nbO > 0) aOuv = true;
                for (int k = 0; k < nbO; k++) {
                    System.out.print("    Type (1:Porte, 2:Fenetre) : ");
                    if (Lire.choixBinaire() == 1) m.ajouterOuverture(new Porte(k+1));
                    else m.ajouterOuverture(new Fenetre(k+1));
                }
                appliquerRev(m, "Mur");
                p.ajouterMur(m);
            }

            System.out.print("  Nb tremies SOL : ");
            int nbTS = Lire.entierCompris(0, 5);
            for (int k = 0; k < nbTS; k++) {
                System.out.print("    Surface tremie " + (k+1) + " : ");
                p.getSol().ajouterTremie(new Tremie(k+1, Lire.d()));
            }
            appliquerRev(p.getSol(), "Sol");
            appliquerRev(p.getPlafond(), "Plafond");

            System.out.print("Recommencer cette piece ? (1:Oui, 2:Non) : ");
            if (Lire.choixBinaire() == 2) return p;
        }
    }

    private void appliquerRev(Object obj, String type) {
        System.out.print("  Revetement sur " + type + " ? (1:Oui, 2:Non) : ");
        if (Lire.choixBinaire() == 1) {
            System.out.println("  --- Catalogue " + type + " ---");
            for (int i = 0; i < nbRev; i++) {
                boolean comp = (type.equals("Mur") && catalogue[i].estPourMur()) || 
                               (type.equals("Sol") && catalogue[i].estPourSol()) || 
                               (type.equals("Plafond") && catalogue[i].estPourPlafond());
                if (comp) System.out.println("   ID " + catalogue[i].getId() + " : " + catalogue[i].getDesignation());
            }
            System.out.print("  ID choisi : ");
            int id = Lire.i();
            for (int i = 0; i < nbRev; i++) {
                if (catalogue[i].getId() == id) {
                    if (obj instanceof Mur) ((Mur)obj).appliquerRevetement(catalogue[i]);
                    else if (obj instanceof Sol) ((Sol)obj).appliquerRevetement(catalogue[i]);
                    else ((Plafond)obj).appliquerRevetement(catalogue[i]);
                }
            }
        }
    }
}