/* =========================================================================
 * NOM DE LA CLASSE : Niveau
 * DATE DE MISE À JOUR : Mai 2026
 * CATÉGORIE TECHNIQUE : Classe Métier (Composant du bâtiment)
 * =========================================================================
 * DESCRIPTION EN 2 LIGNES :
 * Représente un étage complet d'un immeuble.
 * Il sert de conteneur pour regrouper plusieurs appartements sur un même palier.
 * =========================================================================
 * @author Siméon
 * =========================================================================
 */
package com.mycompany.projetinfos2.devisbatiment;

public class Niveau {
    private int idNiveau;
    // Hauteur sous plafond (HSP) qui sera appliquée à tous les murs de cet étage
    private double hauteurPlafond;
    // Tableau fixant le nombre maximum d'appartements sur cet étage
    private Appartement[] apparts;
    // Compteur pour suivre le remplissage du tableau
    private int nbA = 0;

    // Constructeur : initialise l'étage avec sa hauteur et sa capacité d'appartements
    public Niveau(int id, double h, int nbrA) {
        this.idNiveau = id; 
        this.hauteurPlafond = h; 
        this.apparts = new Appartement[nbrA];
    }

    // Ajoute un appartement dans le tableau s'il reste de la place
    public void ajouterAppart(Appartement a) { 
        if (nbA < apparts.length) apparts[nbA++] = a; 
    }
    
    // Calcule le prix de tout l'étage en additionnant les devis de chaque appartement
    public double devisNiveau() {
        double t = 0;
        // On passe la hauteur sous plafond aux appartements pour qu'ils puissent calculer la surface de leurs murs
        for (int i = 0; i < nbA; i++) t += apparts[i].devisAppartement(hauteurPlafond);
        return t;
    }

    // Calcule la surface au sol totale de l'étage
    public double surfaceSolNiveau() {
        double t = 0;
        for (int i = 0; i < nbA; i++) t += apparts[i].surfaceSolAppartement();
        return t;
    }
    
    // Getters
    public double getHauteur() { return hauteurPlafond; }
    public int getId() { return idNiveau; }
    public Appartement[] getApparts() { return apparts; }
}