/* =========================================================================
 * NOM DE LA CLASSE : Tremie
 * DATE DE MISE À JOUR : Mai 2026
 * CATÉGORIE TECHNIQUE : Sous-classe (Héritage de Ouverture)
 * =========================================================================
 * DESCRIPTION EN 2 LIGNES :
 * Représente un "trou" horizontal dans le sol ou le plafond.
 * Utilisé principalement pour le passage des escaliers ou cheminées.
 * =========================================================================
 * @author Clémentine / Siméon
 * =========================================================================
 */
package com.mycompany.projetinfos2.devisbatiment;

public class Tremie extends Ouverture {
    private int idTremie;
    // Contrairement aux portes qui ont une taille standard, la surface d'une trémie est libre
    private double surface;
    
    // Constructeur : on exige l'identifiant ET la surface choisie par l'utilisateur
    public Tremie(int id, double s) { 
        this.idTremie = id; 
        this.surface = s; 
    }
    
    // Renvoie la surface personnalisée
    @Override 
    public double surface() { 
        return surface; 
    }
    
    @Override 
    public String getNom() { 
        return "Tremie"; 
    }
}