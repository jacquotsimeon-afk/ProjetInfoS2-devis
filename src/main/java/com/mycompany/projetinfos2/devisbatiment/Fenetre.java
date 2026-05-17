/* =========================================================================
 * NOM DE LA CLASSE : Fenetre
 * DATE DE MISE À JOUR : Mai 2026
 * CATÉGORIE TECHNIQUE : Sous-classe (Héritage)
 * =========================================================================
 * DESCRIPTION EN 2 LIGNES :
 * Représente une ouverture de type Fenêtre sur un mur. 
 * Définit ses dimensions standards pour pouvoir déduire sa surface à peindre.
 * =========================================================================
 * @author Clémentine / Siméon
 * =========================================================================
 */
package com.mycompany.projetinfos2.devisbatiment;

public class Fenetre extends Ouverture {
    // Numéro d'identification de la fenêtre
    private int idFenetre;

    // Constructeur basique
    public Fenetre(int id) { 
        this.idFenetre = id; 
    }

    // Méthode polymorphe qui renvoie la surface géométrique de l'ouverture
    @Override
    public double surface() {
        return 1.20 * 1.20; // On part sur une dimension standard de 1.20m sur 1.20m
    }

    // Retourne le nom du composant (utile pour les affichages)
    @Override
    public String getNom() { 
        return "Fenetre"; 
    }
}