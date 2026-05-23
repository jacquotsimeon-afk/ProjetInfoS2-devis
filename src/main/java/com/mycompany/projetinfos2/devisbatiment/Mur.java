/* =========================================================================
 * NOM DE LA CLASSE : Mur
 * DATE DE MISE À JOUR : Mai 2026
 * CATÉGORIE TECHNIQUE : Classe Métier (Composant du bâtiment)
 * =========================================================================
 * DESCRIPTION EN 2 LIGNES :
 * Représente une cloison avec un point de départ et de fin.
 * Gère ses propres ouvertures (portes/fenêtres) pour déduire la surface nette à peindre.
 * =========================================================================
 * @author Siméon
 * =========================================================================
 */
package com.mycompany.projetinfos2.devisbatiment;

public class Mur {
    private int idMur;
    // Les deux coordonnées qui définissent le segment du mur sur le plan
    private Coin debut, fin;
    
    // Tableau polymorphe : il peut contenir aussi bien des Portes que des Fenêtres
    private Ouverture[] ouvertures;
    private int nbO = 0;
    
    // Le matériau de finition appliqué sur ce mur (null si le mur est brut)
    private Revetement revetement;

    // Constructeur : on fournit les points de départ et d'arrivée
    public Mur(int id, Coin d, Coin f, int maxO) {
        this.idMur = id; 
        this.debut = d; 
        this.fin = f;
        this.ouvertures = new Ouverture[maxO];
    }

    public void ajouterOuverture(Ouverture o) { 
        if (nbO < ouvertures.length) ouvertures[nbO++] = o; 
    }
    
    public void appliquerRevetement(Revetement r) { 
        this.revetement = r; 
    }

    // Calcul mathématique de la distance entre le point de départ et d'arrivée (Théorème de Pythagore)
    public double longueur() {
        return Math.sqrt(Math.pow(fin.getCx() - debut.getCx(), 2) + Math.pow(fin.getCy() - debut.getCy(), 2));
    }

    // Calcule la surface à peindre : Surface totale (Longueur * Hauteur) MOINS la surface des ouvertures
    public double surface(double h) {
        double s = longueur() * h;
        // On soustrait chaque ouverture
        for (int i = 0; i < nbO; i++) s -= ouvertures[i].surface();
        // Math.max(0, s) évite d'avoir une surface négative si l'utilisateur met trop de portes sur un petit mur
        return Math.max(0, s);
    }

    // Calcule le prix du revêtement appliqué sur la surface nette
    public double devisMur(double h) { 
        // Si un revêtement est appliqué, on calcule son prix, sinon ça coûte 0€
        return (revetement != null) ? revetement.montant(surface(h)) : 0; 
    }

    // Getters
    public Revetement getRevetement() { return revetement; }
    public Coin getDebut() { return debut; }
    public Coin getFin() { return fin; }
    public Ouverture[] getOuvertures() { return ouvertures; }
    public int getNbO() { return nbO; }
}