/* =========================================================================
 * NOM DE LA CLASSE : Revetement
 * DATE DE MISE À JOUR : Mai 2026
 * CATÉGORIE TECHNIQUE : Classe Abstraite (Modèle parent)
 * =========================================================================
 * DESCRIPTION EN 2 LIGNES :
 * Représente un matériau du catalogue (peinture, carrelage, etc.).
 * Définit où il a le droit d'être appliqué (Mur, Sol, Plafond).
 * =========================================================================
 * @author Clémentine / Siméon
 * =========================================================================
 */
package com.mycompany.projetinfos2.devisbatiment;

public abstract class Revetement {
    protected int idRevetement;
    protected String designation;
    protected double prix; // Prix au mètre carré
    
    // Booléens qui agissent comme des autorisations (ex: vrai si on peut le mettre au sol)
    protected boolean pourMur, pourSol, pourPlafond;

    // Chaque sous-classe devra fournir sa propre méthode de calcul de coût
    public abstract double montant(double surface);
    
    // Getters standard
    public int getId() { return idRevetement; }
    public String getDesignation() { return designation; }
    public double getPrix() { return prix; }
    public boolean estPourMur() { return pourMur; }
    public boolean estPourSol() { return pourSol; }
    public boolean estPourPlafond() { return pourPlafond; }
}