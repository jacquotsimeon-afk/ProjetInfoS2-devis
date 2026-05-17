/* =========================================================================
 * NOM DE LA CLASSE : Coin
 * DATE DE MISE À JOUR : Mai 2026
 * CATÉGORIE TECHNIQUE : Classe Métier (Composant géométrique)
 * =========================================================================
 * DESCRIPTION EN 2 LIGNES :
 * Représente un point mathématique (coordonnées X et Y en mètres) sur le plan.
 * Utilisé pour définir le point de départ et le point d'arrivée de chaque mur.
 * =========================================================================
 * @author Clémentine / Siméon
 * =========================================================================
 */
package com.mycompany.projetinfos2.devisbatiment;

public class Coin {
    private int idCoin;
    // Coordonnée horizontale (X) sur le plan en mètres
    private double cx;
    // Coordonnée verticale (Y) sur le plan en mètres
    private double cy;

    // Constructeur pour initialiser la position du point
    public Coin(int id, double x, double y) {
        this.idCoin = id;
        this.cx = x;
        this.cy = y;
    }

    // Getters pour récupérer les coordonnées mathématiques
    public double getCx() { return cx; }
    public double getCy() { return cy; }
    public int getId() { return idCoin; }

    // Méthode utile pour afficher les coordonnées proprement si on fait des tests dans la console
    @Override
    public String toString() {
        return "Point " + idCoin + " (" + cx + "m, " + cy + "m)";
    }
}