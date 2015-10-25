/*
 *     PowerSwitch by Max Rosin & Markus Ressel
 *     Copyright (C) 2015  Markus Ressel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.obj;

import java.util.LinkedList;
import java.util.List;

import eu.power_switch.obj.device.Receiver;

/**
 * Represents a room that can contain receivers
 */
public class Room {

    /**
     * ID of this Room
     */
    private long id;

    /**
     * Name of this Room
     */
    private String name;

    /**
     * List of all receivers that this room contains
     */
    private LinkedList<Receiver> receivers;

    /**
     * Constructor
     *
     * @param id
     * @param name
     */
    public Room(long id, String name) {
        this.id = id;
        this.name = name;
        receivers = new LinkedList<>();
    }

    /**
     * Get all contained receivers
     *
     * @return Receiver list
     */
    public LinkedList<Receiver> getReceivers() {
        return receivers;
    }

    /**
     * Add Receiver to the contained receivers of this Room
     *
     * @param receiver
     */
    public void addReceiver(Receiver receiver) {
        receivers.add(receiver);
    }

    /**
     * Add a List of Receivers to the contained receivers of this Room
     *
     * @param receivers
     */
    public void addReceivers(List<Receiver> receivers) {
        this.receivers.addAll(receivers);
    }

    /**
     * Get ID of this Room
     *
     * @return ID
     */
    public long getId() {
        return id;
    }

    /**
     * Get name of this Room
     *
     * @return Name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets a specific Receiver in this Room
     *
     * @param name Name of Receiver
     * @return Receiver
     */
    public Receiver getReceiver(String name) {
        for (Receiver receiver : receivers) {
            if (receiver.getName().equals(name)) {
                return receiver;
            }
        }
        return null;
    }
}
