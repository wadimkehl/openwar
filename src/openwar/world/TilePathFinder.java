/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import openwar.DB.Army;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;
import openwar.DB.Unit;

/**
 *
 * @author kehl
 */
public class TilePathFinder {

    // Serves for path finding things
    public class PathTile extends Tile {

        public double distance;
        public double heuristic;
        public PathTile ancestor;

        public PathTile(int x, int z, double d, double h, PathTile a) {
            super(x, z);
            distance = d;
            heuristic = h;
            ancestor = a;
        }
    }
    WorldMap map;
    private int max_distance = 150;

    public TilePathFinder(WorldMap m) {
        this.map = m;
    }

    public Stack<Tile> findPath(int sx, int sz, int gx, int gz, boolean walks, boolean sails) {
        return findPath(new Tile(sx, sz), new Tile(gx, gz), walks, sails);
    }

    public Stack<Tile> findPath(Tile start, Tile end, boolean walks, boolean sails) {

        if (walks && !map.walkableTile(end)) {
            return null;
        }

        if (sails && !map.sailableTile(end)) {
            return null;
        }

        double h = Math.sqrt((end.x - start.x) * (end.x - start.x) + (end.z - start.z) * (end.z - start.z));

        if (h > max_distance) {
            return null;
        }


        LinkedList<PathTile> open = new LinkedList<PathTile>();
        LinkedList<PathTile> closed = new LinkedList<PathTile>();
        open.add(new PathTile(start.x, start.z, 0, h, null));

        PathTile p = null;
        while (!open.isEmpty()) {

            // find in open list best candidate (minimal distance) and remove
            double min = 100000;
            PathTile best = null;
            for (PathTile temp : open) {
                if ((temp.distance + temp.heuristic) < min && map.walkableTile(temp)) {
                    min = temp.distance + temp.heuristic;
                    best = temp;
                }
            }
            open.remove(best);


            // check if we reached the goal
            if (best.x == end.x && best.z == end.z) {
                p = best;
                break;
            }

            // expand the candidate, i.e. run through all neighbors and check 'em
            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    int newx = map.ensureInTerrainX(best.x + i);
                    int newz = map.ensureInTerrainZ(best.z + j);

                    // check if already in closed list -> ignore
                    boolean alreadyClosed = false;
                    for (PathTile temp : closed) {
                        if (temp.x == newx && temp.z == newz) {
                            alreadyClosed = true;
                        }
                    }
                    if (alreadyClosed) {
                        continue;
                    }

                    if (walks && !map.walkableTile(newx, newz)) {
                        continue;
                    }

                    if (sails && !map.sailableTile(newx, newz)) {
                        continue;
                    }

                    double new_distance = best.distance + map.getTileCosts(newx, newz);

                    // check if in open list
                    boolean alreadyOpen = false;
                    for (PathTile temp : open) {
                        if (temp.x == newx && temp.z == newz) {
                            alreadyOpen = true;

                            // check if we found a shorter path
                            if (new_distance < temp.distance) {
                                temp.ancestor = best;
                                temp.distance = new_distance;
                            }
                        }
                    }

                    if (!alreadyOpen) {
                        h = Math.sqrt((end.x - newx) * (end.x - newx) + (end.z - newz) * (end.z - newz));
                        open.add(new PathTile(newx, newz, new_distance, h, best));
                    }

                }
            }

            // And finally add to closed list
            closed.add(best);

        }

        // Build the path recursively
        Stack<Tile> path = new Stack<Tile>();
        path.push((Tile) p);
        while (p.ancestor != null) {
            path.push((Tile) p.ancestor);
            p = p.ancestor;
        }
        return path;
    }

    public ArrayList<Tile> getReachableArea(ArrayList<Unit> units, int posX, int posZ, boolean walks, boolean sails) {
        ArrayList<Tile> area = new ArrayList<Tile>();

        int points = 10000;
        for (Unit u : units) {
            points = Math.min(u.currMovePoints, points);
        }

        if (points <= 0) {
            area.add(new Tile(posX, posZ));
            return area;
        }

        // Holds global distance values discovered yet
        double[][] distance = new double[2 * points][2 * points];
        for (int x = 0; x < 2 * points; x++) {
            for (int z = 0; z < 2 * points; z++) {
                distance[x][z] = 10000;
            }
        }
        distance[points][points] = 0;

        // Do BFS for all tiles in question starting from army's position
        LinkedList<PathTile> q = new LinkedList<PathTile>();
        q.add(new PathTile(posX, posZ, 0, 0, null));
        while (!q.isEmpty()) {

            PathTile t = q.remove();
            for (int x = -1; x < 2; x++) {
                for (int z = -1; z < 2; z++) {

                    if (!map.insideTerrain(t.x + x, t.z + z)) {
                        continue;
                    }

                    if (walks && !map.walkableTile(t.x + x, t.z + z)) {
                        continue;
                    }

                    if (sails && !map.sailableTile(t.x + x, t.z + z)) {
                        continue;
                    }

                    int offset_x = t.x - posX + x + points;
                    int offset_z = t.z - posZ + z + points;
                    if (offset_x < 0 || offset_x > points * 2 - 1 || offset_z < 0 || offset_z > points * 2 - 1) {
                        continue;
                    }

                    double new_d = map.getTileCosts(t.x + x, t.z + z) + t.distance;
                    if (new_d > points) {
                        continue;
                    }
                    if (new_d < distance[offset_x][offset_z]) {
                        distance[offset_x][offset_z] = new_d;
                        q.add(new PathTile(t.x + x, t.z + z, new_d, 0, t));
                    }
                }
            }
        }

        for (int z = -points; z < points; z++) {
            for (int x = -points; x < points; x++) {
                if (distance[points + x][points + z] <= points) {
                    area.add(new Tile(posX + x, posZ + z));
                }
            }
        }

        return area;
    }

    public ArrayList<Tile> getReachableArea(Army army, boolean walks, boolean sails) {

        return getReachableArea(army.units, army.posX, army.posZ, walks, sails);
    }
}
