/* =========================================================================
 * NOM DE LA CLASSE : Piece
 * DATE DE MISE À JOUR : Mai 2026
 * CATÉGORIE TECHNIQUE : Classe Métier (Composant du bâtiment)
 * =========================================================================
 * DESCRIPTION EN 2 LIGNES :
 * Le cœur du bâtiment. Une pièce contient des murs, un sol et un plafond.
 * Gère sa surface brute et additionne les devis de toutes ses surfaces.
 * =========================================================================
 * @author Siméon
 * =========================================================================
 */
package com.mycompany.projetinfos2.devisbatiment;

public class Piece {
    private int idPiece;
    // Tableau contenant les murs (cloisons) de la pièce
    private Mur[] murs;
    private int nbM = 0;
    private String nom; //Nom de la pièce
    
    // Instanciation directe d'un sol et d'un plafond dès la création de la pièce
    private Sol sol = new Sol();
    private Plafond plafond = new Plafond();

    public Piece(int id, int nbrMurs) {
        this.idPiece = id;
        this.murs = new Mur[nbrMurs];
        this.nom = "Pièce n°" + id;
    }

    public void ajouterMur(Mur m) { 
        if (nbM < murs.length) murs[nbM++] = m; 
    }
    
    // Calcule la surface au sol brute (Longueur * Largeur) en se basant sur les deux premiers murs
    public double surfaceSol() {
        if (nbM < 2) return 0; // Sécurité : il faut au moins 2 murs pour faire une surface
        return murs[0].longueur() * murs[1].longueur();
    }

    // Additionne le prix des revêtements du sol, du plafond, et de tous les murs
    public double devisPiece(double h) {
        // On commence par le prix du sol et du plafond
        double total = sol.devis(surfaceSol()) + plafond.devis(surfaceSol());
        // On y ajoute le prix de chaque mur (qui dépend de la hauteur h)
        for (int i = 0; i < nbM; i++) total += murs[i].devisMur(h);
        return total;
    }

    // Getters // Setters
    public void setNom(String n) { this.nom = n; }
    public String getNom() { return nom; }
    public Mur[] getMurs() { return murs; }
    public Sol getSol() { return sol; }
    public Plafond getPlafond() { return plafond; }
    public int getId() { return idPiece; }
}