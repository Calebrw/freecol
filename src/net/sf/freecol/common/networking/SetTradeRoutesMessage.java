/**
 *  Copyright (C) 2002-2016   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.common.networking;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.sf.freecol.common.i18n.Messages;
import net.sf.freecol.common.model.FreeColObject;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.StringTemplate;
import net.sf.freecol.common.model.TradeRoute;
import net.sf.freecol.server.FreeColServer;
import net.sf.freecol.server.model.ServerPlayer;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * The message sent when setting the trade routes.
 */
public class SetTradeRoutesMessage extends DOMMessage {

    public static final String TAG = "setTradeRoutes";

    /** The trade routes to set. */
    private final List<TradeRoute> tradeRoutes = new ArrayList<>();


    /**
     * Create a new <code>SetTradeRoutesMessage</code> with the
     * supplied routes.
     *
     * @param tradeRoutes A list of <code>TradeRoute</code>s to set.
     */
    public SetTradeRoutesMessage(List<TradeRoute> tradeRoutes) {
        super(getTagName());

        this.tradeRoutes.clear();
        if (tradeRoutes != null) this.tradeRoutes.addAll(tradeRoutes);
    }

    /**
     * Create a new <code>SetTradeRoutesMessage</code> from a
     * supplied element.
     *
     * @param game The <code>Game</code> this message belongs to.
     * @param element The <code>Element</code> to use to create the message.
     */
    public SetTradeRoutesMessage(Game game, Element element) {
        this(null);

        this.tradeRoutes.addAll(DOMMessage.mapChildren(game, element,
                e -> DOMMessage.readGameElement(game, e, false,
                                                TradeRoute.class)));
    }


    /**
     * Handle a "setTradeRoutes"-message.
     *
     * @param server The <code>FreeColServer</code> handling the message.
     * @param connection The <code>Connection</code> message was received on.
     * @return Null, or an error <code>Element</code> on failure.
     */
    public Element handle(FreeColServer server, Connection connection) {
        final ServerPlayer serverPlayer = server.getPlayer(connection);
        final Game game = server.getGame();

        StringTemplate fail = StringTemplate.label(", ");
        List<TradeRoute> newRoutes = new ArrayList<>();
        for (TradeRoute tr : tradeRoutes) {
            StringTemplate st = tr.verify(false);
            if (st != null) {
                fail.addStringTemplate(st);
            } else {
                newRoutes.add(tr);
            }
        }
        if (!fail.isEmpty()) {
            return serverPlayer.clientError(Messages.message(fail))
                .build(serverPlayer);
        }

        // Proceed to set trade routes
        return server.getInGameController()
            .setTradeRoutes(serverPlayer, newRoutes)
            .build(serverPlayer);
    }

    /**
     * Convert this SetTradeRoutesMessage to XML.
     *
     * @return The XML representation of this message.
     */
    @Override
    public Element toXMLElement() {
        return new DOMMessage(getTagName())
            .add(this.tradeRoutes).toXMLElement();
    }

    /**
     * The tag name of the root element representing this object.
     *
     * @return "setTradeRoutes".
     */
    public static String getTagName() {
        return TAG;
    }
}
