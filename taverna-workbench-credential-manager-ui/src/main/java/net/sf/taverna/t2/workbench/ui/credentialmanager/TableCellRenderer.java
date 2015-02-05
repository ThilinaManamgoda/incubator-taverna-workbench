/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.ui.credentialmanager;

import static net.sf.taverna.t2.workbench.ui.credentialmanager.CredentialManagerUI.KEY_PAIR_ENTRY_TYPE;
import static net.sf.taverna.t2.workbench.ui.credentialmanager.CredentialManagerUI.PASSWORD_ENTRY_TYPE;
import static net.sf.taverna.t2.workbench.ui.credentialmanager.CredentialManagerUI.TRUST_CERT_ENTRY_TYPE;

import java.awt.Component;
//import java.text.DateFormat;
//import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
//import net.sf.taverna.t2.workbench.ui.credentialmanager.KeyPairsTableModel;
//import net.sf.taverna.t2.workbench.ui.credentialmanager.PasswordsTableModel;
//import net.sf.taverna.t2.workbench.ui.credentialmanager.TrustedCertsTableModel;

/**
 * Custom cell renderer for the cells of the tables displaying
 * Keystore/Truststore contents.
 * 
 * @author Alex Nenadic
 */
public class TableCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = -3983986682794010259L;

	private final ImageIcon passwordEntryIcon = new ImageIcon(
			TableCellRenderer.class.getResource("/images/table/key_entry.png"));
	private final ImageIcon keypairEntryIcon = new ImageIcon(
			TableCellRenderer.class
					.getResource("/images/table/keypair_entry.png"));
	private final ImageIcon trustcertEntryIcon = new ImageIcon(
			TableCellRenderer.class
					.getResource("/images/table/trustcert_entry.png"));

	/**
	 * Get the rendered cell for the supplied value and column.
	 */
	@Override
	public Component getTableCellRendererComponent(JTable keyStoreTable,
			Object value, boolean bIsSelected, boolean bHasFocus, int iRow,
			int iCol) {
		JLabel cell = (JLabel) super.getTableCellRendererComponent(
				keyStoreTable, value, bIsSelected, bHasFocus, iRow, iCol);

		if (value != null) {
        	// Type column - display an icon representing the type
			if (iCol == 0)
				configureTypeColumn(value, cell);
            // Last Modified column - format date (if date supplied)        
            /*else if (((keyStoreTable.getModel() instanceof PasswordsTableModel) && (iCol == 3)) || 
            	((keyStoreTable.getModel() instanceof KeyPairsTableModel) && (iCol == 4))||
            	((keyStoreTable.getModel() instanceof TrustedCertsTableModel) && (iCol == 4))){
            	if (value instanceof Date) {
            		// Include timezone
            		cell.setText(DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
            			DateFormat.LONG).format((Date) value));
            	} else {
            		cell.setText(value.toString());
            	}
            }*/
            // Other columns - just use their text values
			else
				cell.setText(value.toString());
		}

		cell.setBorder(new EmptyBorder(0, 5, 0, 5));
		return cell;
	}

	private void configureTypeColumn(Object value, JLabel cell) {
		ImageIcon icon = null;
		// The cell is in the first column of Passwords table
		if (PASSWORD_ENTRY_TYPE.equals(value)) {
			icon = passwordEntryIcon; // key (i.e. password) entry image
		}
		// The cell is in the first column of Key Pairs table
		else if (KEY_PAIR_ENTRY_TYPE.equals(value)) {
			icon = keypairEntryIcon; // key pair entry image
		}
		// The cell is in the first column of Trusted Certificates table
		else if (TRUST_CERT_ENTRY_TYPE.equals(value)) {
			icon = trustcertEntryIcon; // trust. certificate entry image
		}

		cell.setIcon(icon);
		cell.setText("");
		cell.setVerticalAlignment(CENTER);
		cell.setHorizontalAlignment(CENTER);
	}
}