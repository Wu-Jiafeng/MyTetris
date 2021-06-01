import java.util.*;
import java.io.*;

/**
 * The {@code HW1_1700017733} class wraps a value of the primitive type {@code
 * HW1_1700017733} in an object. An object of type {@code HW1_1700017733} contains a
 * single field whose type is {@code HW1_1700017733}.
 *
 * <p> In addition, this class provides several methods for RobotAI with
 *  the improved Pierre Dellacherie’s Algorithm to play Tetris.
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
	public Queue<PieceOperator> op_queue=new LinkedList<PieceOperator>(); //存一个操作队列？每当有新方块出现或空队列就更新?
	public int op_sum=0; //总操作数
	
	/**
	 * Calculate the Landing Height:  
	 * =piece的（最高点+最低点）/2
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
	 * =消除的行数和此方块中参与到行消除的方块数目的乘积
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
	 * 每一行中，方块有或无的变化次数之和。从有方块的格子到相邻的空白格计入一次变换。其中，边框算作有方块。注意，不仅仅是计算底部方块堆叠的区域，上方空白区域也要计算。
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
	 * 每一列中，方块有或无的变化次数之和。从有方块的格子到相邻的空白格计入一次变换。其中，边框算作有方块。注意，不仅仅是计算底部方块堆叠的区域，上方空白区域也要计算。
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
	 * 空洞的数量。空洞指的是，每列中某个方块下面没有方块的空白位置，该空白可能由 1 个单位或多个单位组成，但只要没有被方块隔断，都只算一个空洞。
	 * 注意，空洞的计算以列为单位，若不同列的相邻空格连在一起，不可以将它们算作同一个空洞。
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
	 * 井的连加和。井指的是某一列中，两边都有方块的连续空格，边框算作有方块。
	 * @param tmp_board the {@code boolean[][]}
	 * @return the Well Sum (=sum)
	 */
	public int CalWSum(boolean tmp_board[][]) {
		int sum = 0,cnt = 0;
	    int well[] = {0, 1, 3, 6, 10, 15, 21, 28, 36, 45, 55, 66, 78, 91, 105,
	        120, 136, 153, 171, 190, 210};        //井的打表
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
	 * 通过模拟drop操作确定是否已经land
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
	 * 通过检查边界缓冲区确定是否已经结束
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
	 * 由于操作需要，这里只实现了经单次rotate的旋转操作
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
			op_queue.offer(PieceOperator.Rotate); 	//添加旋转操作
			op_sum=(++op_sum)%5;
			if(op_sum==0) undrop++;
		}
		PieceOperator move_x=(getPieceX()<best_piece_x)?PieceOperator.ShiftRight:PieceOperator.ShiftLeft; //根据x相对位置判断移动方向
		for(int i=Math.min(getPieceX(), best_piece_x);i<Math.max(getPieceX(), best_piece_x);++i) {
			op_queue.offer(move_x); //添加x移动操作
			op_sum=(++op_sum)%5;
			if(op_sum==0) undrop++;
		}
		for(int i=best_piece_y+undrop;i<getPieceY();i++) {
			op_queue.offer(PieceOperator.Drop);					    //添加下落操作
			op_sum=(++op_sum)%5;
		}
	}
	
	/**
	 * Tetris's RobotAI with the improved Pierre Dellacherie’s Algorithm
	 * 对当前方块产生时的初始情况，遍历其落地时的最终旋转情况与实际落点，根据评估函数得出得分，将最优得分所对应的操作加入操作队列
	 * 每次行动时若队列非空则弹出队列第一个元素进行操作
	 * 若队列为空（这与drop_alarm更新时延有关），则返回keep进行操作
	 * @param 
	 * @return op_queue.poll() the {@code PieceOperator} if op_queue is not empty else PieceOperator.Keep the {@code PieceOperator}
	 */
	public PieceOperator robotPlay() {
		if(op_queue.isEmpty()) {
			int best_rotate_times=0; 							//对于当前方块的最佳旋转次数
			int best_piece_x=getPieceX(),best_piece_y=getPieceY(); 		//对于当前方块的最佳落点
			double MaxScore=Double.NEGATIVE_INFINITY;
			boolean tmp_piece[][]=getPiece();
			for(int tmp_rotate_times=0;tmp_rotate_times<=3;tmp_rotate_times++) { 					//方块旋转次数
				if(tmp_rotate_times >0) tmp_piece=getPiece_rotate(tmp_piece); 						// 获得旋转后的方块
				for(int tmp_piece_x=-1;tmp_piece_x<w;tmp_piece_x++) { 								//枚举情况下方块最终落点的x值
					int tmp_piece_y=h-1;															//枚举情况下方块最终落点的y值
					boolean tmp_board[][]=getBoard_removepiece();
					while(IsToLand(tmp_piece_x,tmp_piece_y,tmp_piece,tmp_board)) tmp_piece_y--;		//通过下降确定落点y
					if(tmp_piece_y==h-1) continue;
					tmp_board=getBoard_land(tmp_piece_x,tmp_piece_y+1,tmp_piece,tmp_board); 		//该落点所对应的board
					if(IsGameOver(tmp_board)) continue; 											//如果已经gameover，则剪枝
					double Score=CalScore(tmp_piece_x,tmp_piece_y+1,tmp_piece,tmp_board);
					if(MaxScore<Score) {
						MaxScore=Score;
						best_piece_x=tmp_piece_x;
						best_piece_y=tmp_piece_y+1;
						best_rotate_times=tmp_rotate_times; //更新最优策略
					}
				}
			}
			setOpQueue(best_piece_x,best_piece_y,best_rotate_times);
		}
		PieceOperator op=op_queue.poll();
		if(op==null) { 
			op_sum=(++op_sum)%5; //对齐方块landed后的操作计数
			return PieceOperator.Keep;
		}
		return op;
	}
}