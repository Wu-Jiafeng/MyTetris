import java.util.*;
import java.io.*;

/**
 * The {@code HW1_1700017733} class wraps a value of the primitive type {@code
 * HW1_1700017733} in an object. An object of type {@code HW1_1700017733} contains a
 * single field whose type is {@code HW1_1700017733}.
 *
 * <p> In addition, this class provides several methods for RobotAI with
 *  the improved Pierre Dellacherie��s Algorithm to play Tetris.
 *  
 *  <p> The details for the AI algorithm can be seen at
 *   {@link https://imake.ninja/el-tetris-an-improvement-on-pierre-dellacheries-algorithm/}
 *	
 *	<p> Reference: {@link https://blog.csdn.net/Originum/article/details/81570042}
 *
 * @author  Jiafeng Wu
 * @since   JDK1.8
 * @version 1.0
 */

public class HW1_1700017733 extends Tetris {
	// enter your student id here
	public String id = new String("1700017733");
	public Queue<PieceOperator> op_queue=new LinkedList<PieceOperator>(); //��һ���������У�ÿ�����·�����ֻ�ն��о͸���?
	public int op_sum=0; //�ܲ�����
	
	/**
	 * Calculate the Landing Height:  
	 * =piece�ģ���ߵ�+��͵㣩/2
	 * @param tmp_piece_y the {@code int}, tmp_piece the {@code boolean[][]}
	 * @return the Landing Height (=(MaxHeight+MinHeight)/2)
	 */
	public double CalLHeight(int tmp_piece_y,boolean tmp_piece[][]) {
		ArrayList<Integer> pieceY=getPiecesY(tmp_piece_y,tmp_piece);
		double MaxHeight=Collections.max(pieceY),MinHeight=Collections.min(pieceY);
		return (MaxHeight+MinHeight)/2;
	}
	
	/**
	 * Calculate the Rows eliminated and remove the full lines for tmp_board: 
	 * =�����������ʹ˷����в��뵽�������ķ�����Ŀ�ĳ˻�
	 * @param tmp_piece_x the {@code int}, tmp_piece_y the {@code int}, tmp_piece the {@code boolean[][]}, tmp_board the {@code boolean[][]}
	 * @return the Rows eliminated (=rows*cells)
	 */
	public int CalRowsE(int tmp_piece_x,int tmp_piece_y,boolean tmp_piece[][],boolean tmp_board[][]) {
		int rows=0,cells=0;
		ArrayList<Integer> pieceY=getPiecesY(tmp_piece_y,tmp_piece);
		for (int y = 0; y < h-nBufferLines; y++) {
			boolean full = true;
			for (int x = 0; x < w; x++) {
				if (!tmp_board[y][x]) {
					full = false;
					break;
				}
			}
			if (full) {
				rows++;
				for (int i = y; i < h-nBufferLines; i++) {
					for (int j = 0; j < w; j++) {
						tmp_board[i][j] = tmp_board[i+1][j];
						tmp_board[i+1][j] = false;
					}
				}
				for (int value:pieceY) {
				    if(value==y) cells++;
				    if(value>=y) value--;
				}
				y--;
			}
		}
		return rows*cells;
	}
	
	/**
	 * Calculate the Row Transitions: 
	 * ÿһ���У������л��޵ı仯����֮�͡����з���ĸ��ӵ����ڵĿհ׸����һ�α任�����У��߿������з��顣ע�⣬�������Ǽ���ײ�����ѵ��������Ϸ��հ�����ҲҪ���㡣
	 * @param tmp_board the {@code boolean[][]}
	 * @return the Rows Transitions (=sum)
	 */
	public int CalRowsT(boolean tmp_board[][]) {
		int sum=0;
		for(int y=0;y<h-nBufferLines;y++) {
			if(!tmp_board[y][0]) sum++;
			if(!tmp_board[y][w-1]) sum++;
			for(int x=0;x<w-1;x++) {
				if(tmp_board[y][x]^tmp_board[y][x+1]) sum++;
			}
		}
		return sum;
	}
	
	/**
	 * Calculate the Column Transitions: 
	 * ÿһ���У������л��޵ı仯����֮�͡����з���ĸ��ӵ����ڵĿհ׸����һ�α任�����У��߿������з��顣ע�⣬�������Ǽ���ײ�����ѵ��������Ϸ��հ�����ҲҪ���㡣
	 * @param tmp_board the {@code boolean[][]}
	 * @return the Columns Transitions (=sum)
	 */
	public int CalColumnsT(boolean tmp_board[][]) {
		int sum=0;
		for(int x=0;x<w;x++) {
			if(!tmp_board[0][x]) sum++;
			if(!tmp_board[h-nBufferLines-1][x]) sum++;
			for(int y=0;y<h-nBufferLines-1;y++) {
				if(tmp_board[y][x]^tmp_board[y+1][x]) sum++;
			}
		}
		return sum;
	}
	
	/**
	 * Calculate the Number of Holes: 
	 * �ն����������ն�ָ���ǣ�ÿ����ĳ����������û�з���Ŀհ�λ�ã��ÿհ׿����� 1 ����λ������λ��ɣ���ֻҪû�б�������ϣ���ֻ��һ���ն���
	 * ע�⣬�ն��ļ�������Ϊ��λ������ͬ�е����ڿո�����һ�𣬲����Խ���������ͬһ���ն���
	 * @param tmp_board the {@code boolean[][]}
	 * @return the Number of Holes (=sum)
	 */
	public int CalHNum(boolean tmp_board[][]) {
		int sum=0;
		for(int x=0;x<w;x++) {
			for(int y=h-nBufferLines-1;y>0;y--) {
				if(tmp_board[y][x]&&(!tmp_board[y-1][x])) sum++;
			}
		}
		return sum;
	}
	
	/**
	 * Calculate the Well Sum: 
	 * �������Ӻ͡���ָ����ĳһ���У����߶��з���������ո񣬱߿������з��顣
	 * @param tmp_board the {@code boolean[][]}
	 * @return the Well Sum (=sum)
	 */
	public int CalWSum(boolean tmp_board[][]) {
		int sum = 0,cnt = 0;
	    int well[] = {0, 1, 3, 6, 10, 15, 21, 28, 36, 45, 55, 66, 78, 91, 105,
	        120, 136, 153, 171, 190, 210};        //���Ĵ��
	    for(int x = 0; x < w; x ++) {
	        for(int y = 0; y < h-nBufferLines; y++) {
	        	if(x==0) {
	        		if(!tmp_board[y][x] && tmp_board[y][x+1]) cnt++;
	        		else {
	        			sum += well[cnt];
		                cnt = 0;
	        		}
	        	}
	        	else if(x==w-1) {
	        		if(!tmp_board[y][x] && tmp_board[y][x-1]) cnt++;
	        		else {
	        			sum += well[cnt];
		                cnt = 0;
	        		}
	        	}
	        	else {
	        		if(!tmp_board[y][x] && tmp_board[y][x-1] && tmp_board[y][x+1]) cnt ++;
	        		else{
	        			sum += well[cnt];
		                cnt = 0;
	        		}
	        	}
	        }
	    }
		return sum;
	}
	
	/**
	 * Calculate the Score for each situation: 
	 * The evaluation function is a linear sum of all the above features. 
	 * The weights of each feature were set and determined using particle swarm optimization.
	 * @param tmp_piece_x the {@code int}, tmp_piece_y the {@code int}, tmp_piece the {@code boolean[][]}, tmp_board the {@code boolean[][]}
	 * @return the Score
	 */
	public double CalScore(int tmp_piece_x,int tmp_piece_y,boolean tmp_piece[][],boolean tmp_board[][]) {
		int RowsE=CalRowsE(tmp_piece_x,tmp_piece_y,tmp_piece,tmp_board);
		return -4.500158825082766*CalLHeight(tmp_piece_y,tmp_piece)+3.4181268101392694*RowsE-
				3.2178882868487753*CalRowsT(tmp_board)-9.348695305445199*CalColumnsT(tmp_board)-
				7.899265427351652*CalHNum(tmp_board)-3.3855972247263626*CalWSum(tmp_board);
	}
	
	/**
	 * Determine the final landing place of the piece for this situation: 
	 * ͨ��ģ��drop����ȷ���Ƿ��Ѿ�land
	 * @param tmp_piece_x the {@code int}, tmp_piece_y the {@code int}, tmp_piece the {@code boolean[][]}, tmp_board the {@code boolean[][]}
	 * @return {@code true} if the piece could continue to drop else {@code false}
	 */
	public boolean IsToLand(int tmp_piece_x,int tmp_piece_y,boolean tmp_piece[][],boolean tmp_board[][]){
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				if (!tmp_piece[y][x]) continue;
				if (tmp_piece_x+x < 0 || tmp_piece_x+x >= w
				 || tmp_piece_y-y < 0 || tmp_piece_y-y >= h
				 || tmp_board[tmp_piece_y-y][tmp_piece_x+x]) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Determine whether the game is over for this situation: 
	 * ͨ�����߽绺����ȷ���Ƿ��Ѿ�����
	 * @param tmp_board the {@code boolean[][]}
	 * @return {@code true} if there're pieces in the BufferPlace else {@code false}
	 */
	public boolean IsGameOver(boolean tmp_board[][]) {
		for (int y = h-nBufferLines; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (tmp_board[y][x])   // game over
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Get the tmp_board for this situation at the time when the piece has landed: 
	 * @param tmp_piece_x the {@code int}, tmp_piece_y the {@code int}, tmp_piece the {@code boolean[][]}, tmp_board the {@code boolean[][]}
	 * @return tmp_board the {@code boolean[][]}
	 */
	public boolean[][] getBoard_land(int tmp_piece_x,int tmp_piece_y,boolean tmp_piece[][],boolean tmp_board[][]){
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				if (tmp_piece[y][x]) tmp_board[tmp_piece_y-y][tmp_piece_x+x] = true; 	// deploy piece
			}
		}
		return tmp_board;
	}
	
	/**
	 * Get the tmp_board for this situation at the time when the piece has been removed: 
	 * @param
	 * @return tmp_board the {@code boolean[][]}
	 */
	public boolean[][] getBoard_removepiece(){
		boolean tmp_board[][]=getBoard();
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				if (piece[y][x]) tmp_board[piece_y-y][piece_x+x] = false; 			 	// remove piece from board
			}
		}
		return tmp_board;
	}
	
	/**
	 * Get the tmp_piece rotated: 
	 * ���ڲ�����Ҫ������ֻʵ���˾�����rotate����ת����
	 * @param r_piece the {@code boolean[][]}
	 * @return tmp the {@code boolean[][]}
	 */
	public boolean[][] getPiece_rotate(boolean r_piece[][]){
		boolean tmp[][] = new boolean[4][4];
		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 4; x++) {
				tmp[y][x] = r_piece[x][3-y];
			}
		}
		return tmp;
	}
	
	/**
	 * Get the actual y for each cell in the tmp_piece: 
	 * @param tmp_piece_y the {@code int}, tmp_piece the {@code boolean[][]}
	 * @return pieceY the {@code ArrayList<Integer>}
	 */
	public ArrayList<Integer> getPiecesY(int tmp_piece_y,boolean tmp_piece[][]) {
		ArrayList<Integer> pieceY = new ArrayList<>();
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				if(tmp_piece[y][x]) {
					pieceY.add(tmp_piece_y-y);
				}
			}
		}
		return pieceY;
	}
	
	/**
	 * Update the opQueue according to the max score: 
	 * @param best_piece_x the {@code int}, best_piece_y the {@code int}, best_rotate_times the {@code int}
	 * @return 
	 */
	public void setOpQueue(int best_piece_x,int best_piece_y,int best_rotate_times) {
		int undrop=0;
		for(int i=0;i<best_rotate_times;++i) {
			op_queue.offer(PieceOperator.Rotate); 	//�����ת����
			op_sum=(++op_sum)%5;
			if(op_sum==0) undrop++;
		}
		PieceOperator move_x=(getPieceX()<best_piece_x)?PieceOperator.ShiftRight:PieceOperator.ShiftLeft; //����x���λ���ж��ƶ�����
		for(int i=Math.min(getPieceX(), best_piece_x);i<Math.max(getPieceX(), best_piece_x);++i) {
			op_queue.offer(move_x); //���x�ƶ�����
			op_sum=(++op_sum)%5;
			if(op_sum==0) undrop++;
		}
		for(int i=best_piece_y+undrop;i<getPieceY();i++) {
			op_queue.offer(PieceOperator.Drop);					    //����������
			op_sum=(++op_sum)%5;
		}
	}
	
	/**
	 * Tetris's RobotAI with the improved Pierre Dellacherie��s Algorithm
	 * �Ե�ǰ�������ʱ�ĳ�ʼ��������������ʱ��������ת�����ʵ����㣬�������������ó��÷֣������ŵ÷�����Ӧ�Ĳ��������������
	 * ÿ���ж�ʱ�����зǿ��򵯳����е�һ��Ԫ�ؽ��в���
	 * ������Ϊ�գ�����drop_alarm����ʱ���йأ����򷵻�keep���в���
	 * @param 
	 * @return op_queue.poll() the {@code PieceOperator} if op_queue is not empty else PieceOperator.Keep the {@code PieceOperator}
	 */
	public PieceOperator robotPlay() {
		if(op_queue.isEmpty()) {
			int best_rotate_times=0; 							//���ڵ�ǰ����������ת����
			int best_piece_x=getPieceX(),best_piece_y=getPieceY(); 		//���ڵ�ǰ�����������
			double MaxScore=Double.NEGATIVE_INFINITY;
			boolean tmp_piece[][]=getPiece();
			for(int tmp_rotate_times=0;tmp_rotate_times<=3;tmp_rotate_times++) { 					//������ת����
				if(tmp_rotate_times >0) tmp_piece=getPiece_rotate(tmp_piece); 						// �����ת��ķ���
				for(int tmp_piece_x=-1;tmp_piece_x<w;tmp_piece_x++) { 								//ö������·�����������xֵ
					int tmp_piece_y=h-1;															//ö������·�����������yֵ
					boolean tmp_board[][]=getBoard_removepiece();
					while(IsToLand(tmp_piece_x,tmp_piece_y,tmp_piece,tmp_board)) tmp_piece_y--;		//ͨ���½�ȷ�����y
					if(tmp_piece_y==h-1) continue;
					tmp_board=getBoard_land(tmp_piece_x,tmp_piece_y+1,tmp_piece,tmp_board); 		//���������Ӧ��board
					if(IsGameOver(tmp_board)) continue; 											//����Ѿ�gameover�����֦
					double Score=CalScore(tmp_piece_x,tmp_piece_y+1,tmp_piece,tmp_board);
					if(MaxScore<Score) {
						MaxScore=Score;
						best_piece_x=tmp_piece_x;
						best_piece_y=tmp_piece_y+1;
						best_rotate_times=tmp_rotate_times; //�������Ų���
					}
				}
			}
			setOpQueue(best_piece_x,best_piece_y,best_rotate_times);
		}
		PieceOperator op=op_queue.poll();
		if(op==null) { 
			op_sum=(++op_sum)%5; //���뷽��landed��Ĳ�������
			return PieceOperator.Keep;
		}
		return op;
	}
}