/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

/**
 *
 * @author kehl
 */
public class PathFinder {

    // Serves for path finding things
    public class PathTile extends Tile {

        public int distance;
        public PathTile ancestor;

        public PathTile(int x, int z, int d, PathTile a) {
            super(x, z);
            distance = d;
            ancestor = a;
        }
    }
    WorldMap map;

    public PathFinder(WorldMap m) {
        this.map = m;
    }

    public Stack<Tile> findPath(Tile start, Tile end) {

        if (!map.walkableTile(start) || !map.walkableTile(end)) {
            return null;
        }

        LinkedList<PathTile> open = new LinkedList<PathTile>();
        LinkedList<PathTile> closed = new LinkedList<PathTile>();
        open.add(new PathTile(start.x, start.z, 0, null));

        PathTile p = null;
        while (!open.isEmpty()) {

            // find in open list best candidate (minimal distance) and remove
            int min = 100000;
            PathTile best = null;
            for (PathTile temp : open) {
                if (temp.distance < min && map.walkableTile(temp)) {
                    min = temp.distance;
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

                    int new_distance = best.distance + map.worldTiles[newx][newz].cost;

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
                        open.add(new PathTile(newx, newz, new_distance, best));
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

    public ArrayList<Tile> getReachableArea(Army army) {
        ArrayList<Tile> area = new ArrayList<Tile>();

        int points = army.currMovePoints;
        if (points <= 0) {
            area.add(new Tile(army.posX, army.posZ));
            return area;
        }

        // Holds global distance values discovered yet
        int[][] distance = new int[2 * points][2 * points];
        for (int x = 0; x < 2 * points; x++) {
            for (int z = 0; z < 2 * points; z++) {
                distance[x][z] = 10000;
            }
        }
        distance[points][points] = 0;

        // Do BFS for all tiles in question starting from army's position
        LinkedList<PathTile> q = new LinkedList<PathTile>();
        q.add(new PathTile(army.posX, army.posZ, 0, null));
        while (!q.isEmpty()) {

            PathTile t = q.remove();
            for (int x = -1; x < 2; x++) {
                for (int z = -1; z < 2; z++) {

                    if (!map.insideTerrain(t.x + x, t.z + z)) {
                        continue;
                    }

                    if (!map.walkableTile(t.x + x, t.z + z)) {
                        continue;
                    }

                    int offset_x = t.x - army.posX + x + points;
                    int offset_z = t.z - army.posZ + z + points;
                    if (offset_x < 0 || offset_x > points * 2 - 1 || offset_z < 0 || offset_z > points * 2 - 1) {
                        continue;
                    }

                    int new_d = map.getTileCosts(t.x + x, t.z + z) + t.distance;
                    if (new_d >= points) {
                        continue;
                    }
                    if (new_d < distance[offset_x][offset_z]) {
                        distance[offset_x][offset_z] = new_d;
                        q.add(new PathTile(t.x + x, t.z + z, new_d, t));
                    }
                }
            }
        }

        for (int z = -points; z < points; z++) {
            for (int x = -points; x < points; x++) {
                if (distance[points + x][points + z] <= points)  {
                    area.add(new Tile(army.posX + x, army.posZ + z));
                }
            }
        }

        return area;
    }
}
