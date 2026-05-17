/* =========================================================================
 * NOM DE LA CLASSE : Porte
 * DATE DE MISE À JOUR : Mai 2026
 * CATÉGORIE TECHNIQUE : Sous-classe (Héritage de Ouverture)
 * =========================================================================
 * DESCRIPTION EN 2 LIGNES :
 * Représente une ouverture de type Porte sur un mur. 
 * Définit ses dimensions standards pour pouvoir déduire sa surface à peindre.
 * =========================================================================
 * @author Clémentine / Siméon
 * =========================================================================
 */
package com.mycompany.projetinfos2.devisbatiment;

public class Porte extends Ouverture {
    private int idPorte;
    
    public Porte(int id) { 
        this.idPorte = id; 
    }
    
    // Renvoie la surface d'une porte standard (0.90m de large sur 2.10m de haut)
    @Override 
    public double surface() { 
        return 0.90 * 2.10; 
    }
    
    @Override 
    public String getNom() { 
        return "Porte"; 
    }
}