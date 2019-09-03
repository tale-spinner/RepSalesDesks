import java.awt.EventQueue;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import java.awt.Font;

public class RepSalesDesks extends JFrame {
	private static final long serialVersionUID = 3938118082349145535L;
	
	private JPanel contentPane;
	private JLabel lblInfo;

	DefaultListModel<String> repsModel;
	JList<String> reps;
	
	DefaultListModel<String> desksModel;
	JList<String> desks;
	
	DefaultListModel<String> rep_desksModel;
	JList<String> rep_desks;
	
	TreeMap<String,Integer> mapLoginToUserId = new TreeMap<String,Integer>();
	TreeMap<String,Integer> mapDeskToDeskId  = new TreeMap<String,Integer>();
	
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					RepSalesDesks frame = new RepSalesDesks();
					frame.setVisible(true);
				} catch (Exception e) {
					System.err.println("Uncaught Error! " + e.getMessage());
				}
			}
		});
	}

	
	/**
	 * Create the frame.
	 */
	private RepSalesDesks() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 475, 350);
		contentPane = new JPanel();  // 434 x positions to work with (for some reason)
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		// Initialize map variables
		InitMaps();
		
		
		/*
		 * Setup the list of reps
		 */
		// Get the list of reps and add it to our list model
		repsModel = new DefaultListModel<String>();
		for( String s : mapLoginToUserId.keySet() ) repsModel.addElement(s);
		
		// Setup a JList and add a listener
		reps = new JList<String>( repsModel );
		reps.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		reps.addListSelectionListener(new RepListener());
		
		// Create the scroll pane to contain reps
		JScrollPane repsPane = new JScrollPane( reps );
		repsPane.setBounds(10, 30, 100, 240);
		contentPane.add(repsPane);
		
		// Add a label
		JLabel lblSalesReps = new JLabel("Sales Reps");
		lblSalesReps.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblSalesReps.setBounds(10, 11, 100, 14);
		contentPane.add(lblSalesReps);
		
		
		/*
		 * Setup the list of desks available to be added to selected rep
		 */
		desksModel = new DefaultListModel<String>();
		desks = new JList<String>( desksModel );
		desks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		desks.addListSelectionListener(new AvailDeskListener());
		
		// Create teh scroll pane to contain available desks
		JScrollPane listDesks = new JScrollPane( desks );
		listDesks.setBounds(349, 30, 100, 240);
		contentPane.add(listDesks);
		
		// Add a label
		JLabel lblAvailableDesks = new JLabel("Available Desks");
		lblAvailableDesks.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblAvailableDesks.setBounds(349, 11, 100, 14);
		contentPane.add(lblAvailableDesks);
		
		
		/*
		 * Setup the list of desks the selected rep is associated with
		 */
		rep_desksModel = new DefaultListModel<String>();
		
		rep_desks = new JList<String>(rep_desksModel);
		rep_desks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rep_desks.addListSelectionListener(new CurrDeskListener());
		
		JScrollPane listRepDesks = new JScrollPane( rep_desks );
		listRepDesks.setBounds(115, 30, 100, 240);
		contentPane.add(listRepDesks);
		
		// Add a label
		JLabel lblCurrentDesks = new JLabel("Current Desks");
		lblCurrentDesks.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblCurrentDesks.setBounds(115, 11, 100, 14);
		contentPane.add(lblCurrentDesks);
		
		
		/*
		 *   Setup buttons
		 */
		JButton btnAddAllDesks = new JButton("<< Add All   ");
		btnAddAllDesks.addActionListener(new BtnAddAllDesksListener());
		btnAddAllDesks.setBounds(224, 100, 115, 25);
		contentPane.add(btnAddAllDesks);
		
		JButton btnAddDesk = new JButton("< Add   ");
		btnAddDesk.addActionListener(new BtnAddDeskListener());
		btnAddDesk.setBounds(224, 126, 115, 25);
		contentPane.add(btnAddDesk);
		
		JButton btnRemoveDesk = new JButton("Remove >");
		btnRemoveDesk.addActionListener(new BtnRemoveDeskListener());
		btnRemoveDesk.setBounds(224, 152, 115, 25);
		contentPane.add(btnRemoveDesk);
		
		JButton btnRemoveAllDesks = new JButton("Remove All >>");
		btnRemoveAllDesks.addActionListener(new BtnRemoveAllDesksListener());
		btnRemoveAllDesks.setBounds(224, 178, 115, 25);
		contentPane.add(btnRemoveAllDesks);

		
		/*
		 *   Set a small informational label
		 */
		lblInfo = new JLabel("Informational");
		lblInfo.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblInfo.setHorizontalAlignment(SwingConstants.RIGHT);
		lblInfo.setBounds(10, 280, 439, 14);
		contentPane.add(lblInfo);
	}
	
	private class BtnAddDeskListener implements ActionListener {
		String qry  = "INSERT INTO RepSalesDesks (UserId,SalesTerritoryNameId) VALUES (?,?)";

		@Override
		public void actionPerformed(ActionEvent e) {
			int repIdx  = reps.getSelectedIndex();
			int deskIdx = desks.getSelectedIndex();
			
			if( repIdx == -1 || deskIdx == -1 ) return;

			RunDmlAndReloadLists( qry, mapLoginToUserId.get(repsModel.getElementAt(repIdx)), mapDeskToDeskId.get(desksModel.getElementAt(deskIdx)) );
		}
		
	}
	
	private class BtnAddAllDesksListener implements ActionListener {
		String qry  = "INSERT INTO RepSalesDesks (SalesTerritoryNameId,UserId) "
				+ "    SELECT stn.SalesTerritoryNameId, ?"
				+ "      FROM SalesTerritoryName stn"
				+ "     WHERE NOT EXISTS ( Select NULL"
				+ "                          From RepSalesDesks rd, UsrUsers u"
				+ "                         Where rd.UserId = u.UserId"
				+ "                           And rd.SalesTerritoryNameId = stn.SalesTerritoryNameId"
				+ "                           And u.UserId = ? )"
				+ "    ORDER BY stn.TerritoryName";

		@Override
		public void actionPerformed(ActionEvent e) {
			int repIdx  = reps.getSelectedIndex();
			
			if( repIdx == -1 ) return;
			
			int repId = mapLoginToUserId.get(repsModel.getElementAt(repIdx));

			RunDmlAndReloadLists( qry, repId, repId );
		}
		
	}
	
	private class BtnRemoveDeskListener implements ActionListener {
		String delRepDesk  = "DELETE FROM RepSalesDesks WHERE UserId = ? AND SalesTerritoryNameId = ?";

		@Override
		public void actionPerformed(ActionEvent e) {
			int repIdx  = reps.getSelectedIndex();
			int deskIdx = rep_desks.getSelectedIndex();
			
			if( repIdx == -1 || deskIdx == -1 ) return;

			RunDmlAndReloadLists( delRepDesk, mapLoginToUserId.get(repsModel.getElementAt(repIdx)), mapDeskToDeskId.get(rep_desksModel.getElementAt(deskIdx)) );
		}
	}
	
	private class BtnRemoveAllDesksListener implements ActionListener {
		String delRepDesk  = "DELETE FROM RepSalesDesks WHERE UserId = ?";

		@Override
		public void actionPerformed(ActionEvent e) {
			int repIdx  = reps.getSelectedIndex();
			
			if( repIdx == -1 ) return;

			RunDmlAndReloadLists( delRepDesk, mapLoginToUserId.get(repsModel.getElementAt(repIdx)) );
		}
	}
	
	private void RunDmlAndReloadLists ( String qry, Integer...params ) {
		try {
			Connection con = connect();
			PreparedStatement stmt = con.prepareStatement(qry);
			
			for( int i=0; i<params.length; i++ )
				stmt.setInt(i+1, params[i]);
			
			stmt.execute();
			
			con.close();
			
			LoadDeskList();
			LoadRepDeskList();
		}
		catch(SQLException exc) {
			System.err.println("Error! " + exc.getMessage());
		}
	}

	private class RepListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(e.getValueIsAdjusting()==true) return;
			
			LoadDeskList();
			
			LoadRepDeskList();
		}
	}
	
	private class AvailDeskListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(e.getValueIsAdjusting()==true) return;
			
			// Makes sure we've selected what we think
			if( reps.getSelectedIndex() == -1 || desks.getSelectedIndex() == -1 ) return;
			
			// Set the information label
			SetLblInfo(
					mapLoginToUserId.get( repsModel.get( reps.getSelectedIndex() ) ),
					mapDeskToDeskId.get( desksModel.get( desks.getSelectedIndex() ) )
			);
			
			// If a current desk has been selected, unselect it
			if( !rep_desks.isSelectionEmpty() ) rep_desks.clearSelection();
		}
	}
	
	private class CurrDeskListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(e.getValueIsAdjusting()==true) return;

			// Makes sure we've selected what we think
			if( reps.getSelectedIndex() == -1 || rep_desks.getSelectedIndex() == -1 ) return;

			// Set the information label
			SetLblInfo(
					mapLoginToUserId.get( repsModel.get( reps.getSelectedIndex() ) ),
					mapDeskToDeskId.get( rep_desksModel.get( rep_desks.getSelectedIndex() ) )
			);

			// If an available desk has been selected, unselect it
			if( !desks.isSelectionEmpty() ) desks.clearSelection();
		}
	}
	
	
	private void SetLblInfo( int repId, int deskId ) {
		int numReps = 0;
		
		String qry = "SELECT count(*) FROM RepSalesDesks WHERE UserId != ? AND SalesTerritoryNameId = ?";
		
		try {
			Connection con = connect();
			PreparedStatement stmt = con.prepareStatement(qry);
			stmt.setInt(1, repId);
			stmt.setInt(2, deskId );
			ResultSet rs = stmt.executeQuery();
			
			if(rs.getInt(1) > 0)
				numReps = rs.getInt(1);

			con.close();
		}
		catch (SQLException exc) {
			System.err.println("Exception in LoadDeskList: " + exc.getMessage() );
		}

		
		if( numReps > 0 ) {
			lblInfo.setText( "Desk is used by " + numReps + " other rep(s).");
		}
		else { lblInfo.setText(""); }
	}
	
	
	private Connection connect() {
        String url = "jdbc:sqlite:"+System.getenv("USERPROFILE")+"/Documents/RepSalesDesks.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
        return conn;
	}
	
	private void InitMaps() {
		PreparedStatement initMapsStmt;
		ResultSet results;
		
		Connection con = connect();
		
		/*
		 * Setup rep to user id map
		 */
		try {
			initMapsStmt = con.prepareStatement("SELECT userid,loginname FROM UsrUsers");
			results = initMapsStmt.executeQuery();
			
			while(results.next()) {
				mapLoginToUserId.put( results.getString("loginname"), results.getInt("userid") );
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		 * Setup desk to desk id map
		 */
		try {
			initMapsStmt = con.prepareStatement("SELECT SalesTerritoryNameId, TerritoryName FROM SalesTerritoryName");
			results = initMapsStmt.executeQuery();
			
			while(results.next()) {
				mapDeskToDeskId.put( results.getString("TerritoryName"), results.getInt("SalesTerritoryNameId") );
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try { con.close(); } catch( SQLException e ) { System.err.println("Error in InitMaps while closing connection: " + e.getMessage()); }
	}
	
	private void LoadDeskList() {
		
		int repIdx = reps.getSelectedIndex();
		
		if(repIdx == -1) return;
		
		Connection con = connect();
		
		String qryDesks = "SELECT stn.TerritoryName"
				+ "          FROM SalesTerritoryName stn"
				+ "         WHERE NOT EXISTS ( Select NULL"
				+ "                              From RepSalesDesks rd, UsrUsers u"
				+ "                             Where rd.UserId = u.UserId"
				+ "                               And rd.SalesTerritoryNameId = stn.SalesTerritoryNameId"
				+ "                               And u.UserId = ? )"
				+ "        ORDER BY stn.TerritoryName";
		
		/*
		 * Set the available desk list after we've clicked a rep
		 */
		try {
			// Prepare query
			int repId = mapLoginToUserId.get(repsModel.getElementAt(repIdx));
			PreparedStatement stmt = con.prepareStatement(qryDesks);
			stmt.setInt( 1, repId );
			
			// Run query
			ResultSet rs = stmt.executeQuery();
			
			// Remove any existing elements
			if(!desksModel.isEmpty()) desksModel.removeAllElements();
			lblInfo.setText("");
			
			// Reload
			while(rs.next()) {
				desksModel.addElement(rs.getString("TerritoryName"));
			}
			
			con.close();
		} catch (SQLException e) {
			System.err.println("Exception in LoadDeskList: " + e.getMessage() );
		}
	}
	
	private void LoadRepDeskList() {
		// Check if we selected anything and return if we didn't
		int repIdx = reps.getSelectedIndex();
		if(repIdx == -1) return;
		
		Connection con = connect();
		
		String qryRepDesks = "SELECT stn.TerritoryName"
				+ "             FROM SalesTerritoryName stn, RepSalesDesks rsd"
				+ "            WHERE stn.SalesTerritoryNameId = rsd.SalesTerritoryNameId"
				+ "              AND rsd.UserId = ?"
				+ "           ORDER BY stn.TerritoryName";
		
		
		/*
		 * Set the current desk list
		 */
		try {
			// Prepare query
			int repId = mapLoginToUserId.get(repsModel.getElementAt(repIdx));
			PreparedStatement stmt = con.prepareStatement(qryRepDesks);
			stmt.setInt( 1, repId );
			
			// Execute query
			ResultSet rs = stmt.executeQuery();
			
			// Remove any existing elements
			if(!rep_desksModel.isEmpty()) rep_desksModel.removeAllElements();
			
			// Reload list
			while(rs.next()) {
				rep_desksModel.addElement(rs.getString("TerritoryName"));;
			}
		}
		catch (SQLException e) {
			System.err.println("Exception in LoadRepDeskList: " + e.getMessage() );
		}
	}
}
