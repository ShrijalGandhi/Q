import java.io.*;
import java.text.*;	//to round of double values to the form "##.##"
import java.util.Random;

class pitHoleException extends Exception
{
	public String toString()
	{
	return "Agent hit Wall!!";
	}
}

class State 
{
	int i,j;

	State()
	{
	i=-1;
	j=-1;
	}
}

class Frequency_table
{
	int state[][];

	Frequency_table(int rows,int columns)
	{
	state=new int[rows][columns];

		for(int i=0;i<rows;i++)
		for(int j=0;j<columns;j++)
		state[i][j]=0;
	}
}

class Q_table
{
	double q_value[][];

	Q_table(int rows,int columns)
	{
	q_value=new double[rows][columns];

		for(int i=0;i<rows;i++)
		for(int j=0;j<columns;j++)
		q_value[i][j]=0;
	}
}

public class DataOf_Q_Learnings
{
	static InputStreamReader r;
	static BufferedReader input;

	double world[][],utility[][],initial_utility[][],error[][];
	int world_size_rows,world_size_columns;
	double DISCOUNT;

	String optimal_policy[][];

	final double INFINITY=9999999;
	final double SEED=INFINITY/10;
	final int UP=0;
	final int DOWN=1;
	final int LEFT=2;
	final int RIGHT=3;
	final int CORRECT=0;
	final int MODEL_DIMENSIONS=3;
	final int VISIT_LIMIT=8;
	double ALPHA;

	final double TRANSITION[]=new double[MODEL_DIMENSIONS];

	final int TRANSIT_MATRIX[][]=	{
						{UP,-1,0},
						{DOWN,1,0},
						{LEFT,0,-1},
						{RIGHT,0,1}
					};

	int PROBABILITY_MATRIX[][]=new int[10][10];
	int RANDOM_ROW[][]=new int[10][10];
	int RANDOM_COLUMN[][]=new int[10][10];

	final int ROW_TRANSFORM=1;
	final int COLUMN_TRANSFORM=2;

	int action;
	double reward;

		Random generate;
				
	int iterations=0;		

	State previous_state,current_state;
	Frequency_table N[];
	Q_table Q[];

	DataOf_Q_Learnings()throws IOException
	{
	r=new InputStreamReader(System.in);
	input=new BufferedReader(r);

	generate=new Random();

		current_state=new State();
		previous_state=new State();

		N=new Frequency_table[RIGHT-UP+1];
		Q=new Q_table[RIGHT-UP+1];
		//echo("Welcome to the world of AI");
	}
/*-------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	void driver()throws IOException
	{
		//takeInput();
			fastInput();

		birth_of_agent();
		compute_policy();
		show_policy();
		display_q();
		display_n();


	}
/*-------------------------------------------------------------------------------------------------------------------------------------------------------------*/

	void birth_of_agent()
	{
	int episodes=0;

	random_start();

		while(episodes<200)
		{
		//random_start();
			action=think();
			try_to_do(action);
			Q_learn();
			
				if(previous_state.i==-1)
				{
					random_start();
					episodes++;
				}

		if(learned())
		{
			echo("Done in "+episodes+" episodes");
			return;
		}

		echo("Episodes are "+episodes);
	//	episodes++;
		}
	}
/*-------------------------------------------------------------------------------------------------------------------------------------------------------------*/

	boolean learned()
	{
		compute_policy();
		
			if(optimal_policy[0][0].equalsIgnoreCase("RIGHT"))
			if(optimal_policy[0][1].equalsIgnoreCase("RIGHT"))
			if(optimal_policy[0][2].equalsIgnoreCase("RIGHT"))
			if(optimal_policy[1][0].equalsIgnoreCase("UP"))
			if(optimal_policy[1][2].equalsIgnoreCase("UP"))
			if(optimal_policy[2][0].equalsIgnoreCase("UP") || optimal_policy[2][0].equalsIgnoreCase("RIGHT"))
			if(optimal_policy[2][1].equalsIgnoreCase("RIGHT"))
			if(optimal_policy[2][2].equalsIgnoreCase("UP"))
			if(optimal_policy[2][3].equalsIgnoreCase("LEFT"))
			return true;

	return false;
	}


	void try_to_do(int action)
	{
	int x,y;

			x=generate.nextInt()%10;
			y=generate.nextInt()%10;

		x=x<0?~x:x;
		y=y<0?~y:y;	

//		echo("INTENDED ACTION : "+map(action));

			switch(PROBABILITY_MATRIX[x][y])
			{
			case 0: perform(action);
			break;

			case 1: action=left(action);
				perform(action);
			break;

			case 2: action=right(action);
				perform(action);
			break;
			}

//		echo("EXECUTED ACTION : "+map(action));
	}
/*-------------------------------------------------------------------------------------------------------------------------------------------------------------*/

	void Q_learn()
	{
//	double rewar;

			if(Terminal())
			reward=world[current_state.i][current_state.j]-INFINITY;
			else
			reward=world[current_state.i][current_state.j];

		N[action].state[previous_state.i][previous_state.j]++;

	int F=N[action].state[previous_state.i][previous_state.j];

	double FA=(1.0*ALPHA)/F;
	double Q_sa=Q[action].q_value[previous_state.i][previous_state.j];

	
	//	if(action==2 && previous_state.i==1 && previous_state.j==2)
	//	echo("Updating Q["+map(action)+"]["+previous_state.i+"]["+previous_state.j+"]");
		Q_sa+=FA*(reward-Q_sa+(DISCOUNT)*(getmax()));
		
	Q[action].q_value[previous_state.i][previous_state.j]=Q_sa;




			//if(previous_state.i==0 && previous_state.j==3 || previous_state.i==1 && previous_state.j==3)
			//if(world[previous_state.i][previous_state.j]>10)			
			if(Terminal())			
			{ 
			previous_state.i=-1;
			previous_state.j=-1;
			reward=-1;
			}
			else
			{
			previous_state.i=current_state.i;
			previous_state.j=current_state.j;
			}
	return;
	}

	double getmax()
	{
	double max=-9999.0;
	int best=0;

		for(int i=UP;i<=RIGHT;i++)
		{
			if(Q[i].q_value[current_state.i][current_state.j]>max)
			{
			max=Q[i].q_value[current_state.i][current_state.j];
			best=i;
			}
		}
	//	if(action==2 && previous_state.i==1 && previous_state.j==2)
	//	echo("Using Q["+map(best)+"]["+current_state.i+"]["+current_state.j+"]");

	return max;
	}

/*-------------------------------------------------------------------------------------------------------------------------------------------------------------*/

	int think()
	{
	int best=-1;	
	double MAX=-9999;
	double REWARD[]=new double[4];

			if(all_zero())
			{
			best=generate.nextInt()%4;
			best=best<0?~best:best;
			return best;
			}

		for(int i=UP;i<=RIGHT;i++)
			if(N[i].state[current_state.i][current_state.j]<VISIT_LIMIT)
			{
			REWARD[i]=VISIT_LIMIT/(N[i].state[current_state.i][current_state.j]+1);
			//best=generate.nextInt()%4;
			//best=best<0?~best:best;
			//return best;
			}
			else
			REWARD[i]=Q[i].q_value[current_state.i][current_state.j];

	for(int i=UP;i<=RIGHT;i++)
		if(REWARD[i]>MAX)
		{
		MAX=REWARD[i];
		best=i;
		}

best=generate.nextInt()%4;
best=best<0?~best:best;
/*
int x,y;
			x=generate.nextInt()%10;
			y=generate.nextInt()%10;

		x=x<0?~x:x;
		y=y<0?~y:y;

				switch(PROBABILITY_MATRIX[x][y])
				{
				case 0:
				case 1: return best;
				case 2: best=generate.nextInt()%4;
					best=best<0?~best:best;
					return best;
				}
*/
	return best;
	}

/*-------------------------------------------------------------------------------------------------------------------------------------------------------------*/

	boolean all_zero()
	{
		for(int i=UP;i<=RIGHT;i++)
			if(Q[i].q_value[previous_state.i][previous_state.j]!=0)
			return false;
	return true;
	}	

	double get_max()
	{
	double max=-9999999.0;


	return max;
	}

/*-------------------------------------------------------------------------------------------------------------------------------------------------------------*/

	boolean Terminal()
	{
		if(world[current_state.i][current_state.j] > 5000)
		return true;

	return false;
	}

/*-------------------------------------------------------------------------------------------------------------------------------------------------------------*/

	int left(int action)
	{
		switch(action)
		{
		case UP   : return LEFT;
		case DOWN : return RIGHT;
		case LEFT : return DOWN;
		case RIGHT: return UP;
		}
	
	return -1;
	}

	int right(int action)
	{
		switch(action)
		{
		case UP   : return RIGHT;
		case DOWN : return LEFT;
		case LEFT : return UP;
		case RIGHT: return DOWN;
		}

	return -1;		
	}

/*-------------------------------------------------------------------------------------------------------------------------------------------------------------*/

	void random_start()
	{
		int x,y;

		do
		{
			x=generate.nextInt()%10;
			y=generate.nextInt()%10;

		x=x<0?~x:x;
		y=y<0?~y:y;

			current_state.i=RANDOM_ROW[x][y];
			current_state.j=RANDOM_COLUMN[x][y];

			previous_state.i=current_state.i;
			previous_state.j=current_state.j;
		}
		while(world[current_state.i][current_state.j]==(INFINITY/SEED*(-1)) || world[current_state.i][current_state.j] > SEED);

	//echo("The new state is ["+current_state.i+","+current_state.j+"]");
	}

/*-------------------------------------------------------------------------------------------------------------------------------------------------------------

/*-------------------------------------------------------------------------------------------------------------------------------------------------------------*/

	void perform(int current_action)
	{
//	previous_state.i=current_state.i;
//	previous_state.j=current_state.j;

	int row_transform=-1,column_transform=-1;
	
			try
			{
			row_transform=previous_state.i+TRANSIT_MATRIX[current_action][ROW_TRANSFORM];		//perform the action....what to do for action are hardcoded
			column_transform=previous_state.j+TRANSIT_MATRIX[current_action][COLUMN_TRANSFORM];	//in the TRANSIT_MATRIX

			
				if(world[row_transform][column_transform]==((INFINITY/SEED)*(-1)))
				throw new pitHoleException();

				if(world[row_transform][column_transform]>SEED)
				reward-=INFINITY;

			current_state.i=row_transform;
			current_state.j=column_transform;
			}
			catch(ArrayIndexOutOfBoundsException e)//agent hit wall will remain in same state
			{
			current_state.i=previous_state.i;
			current_state.j=previous_state.j;

			reward=world[current_state.i][current_state.j];
			}
			catch(pitHoleException e) //agent hit wall......will remain in same state
			{
			current_state.i=previous_state.i;
			current_state.j=previous_state.j;

			reward=world[current_state.i][current_state.j];
			}

//	echo("PREVIOUS STATE: ["+previous_state.i+","+previous_state.j+"]");
//	echo("CURRENT  STATE: ["+current_state.i+","+current_state.j+"]");
//	echo("================================");
		
	}
/*-------------------------------------------------------------------------------------------------------------------------------------------------------------*/


/*-------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	String map(int x)
	{
		switch(x)
		{
		case 0:return "UP";
		case 1:return "DOWN";
		case 2:return "LEFT";
		case 3:return "RIGHT";
		}
	return "ERROR";
	}
	void show_world()
	{
		for(int i=0;i<world_size_rows;i++)
		{
			for(int j=0;j<world_size_columns;j++)
			{
				if(world[i][j]>SEED)
				echoc(""+(world[i][j]-INFINITY)+"\t");
				else
				echoc(""+world[i][j]+"\t");
			}
		echo("");
		}
	}

	void show_utility()
	{
	echo("\n\n=============== UTILITY MATRIX =======================\n");
	DecimalFormat d=new DecimalFormat("###.##");
		for(int i=0;i<world_size_rows;i++)
		{
			for(int j=0;j<world_size_columns;j++)
			{
			echoc(""+d.format(utility[i][j])+"\t\t");
			}
		echo("");
		}

	echo("===================================================");
	}

	void show_policy()
	{
	echo("\n\n============== OPTIMUM POLICY =======================\n");

		for(int i=0;i<world_size_rows;i++)
		{
			for(int j=0;j<world_size_columns;j++)
			{
			echoc(""+optimal_policy[i][j]+"\t");
			}
		echo("");
		}

	echo("===================================================");
	}

/*-------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	void fastInput()throws IOException
	{
	
	echo("Welcome to takeInput()");

		world_size_rows=3;
		world_size_columns=4;

			for(int i=UP;i<=RIGHT;i++)
			{
			N[i]=new Frequency_table(world_size_rows,world_size_columns);
			Q[i]=new Q_table(world_size_rows,world_size_columns);
			}
			
			TRANSITION[0]=0.8;
			TRANSITION[1]=0.1;
			TRANSITION[2]=0.1;
		

		for(int i=0;i<10;i++)
		for(int j=0;j<10;j++)
		{
		PROBABILITY_MATRIX[i][j]=-1;
		RANDOM_ROW[i][j]=generate.nextInt()%world_size_rows;
		RANDOM_COLUMN[i][j]=generate.nextInt()%world_size_columns;

			if(RANDOM_ROW[i][j]<0)
			RANDOM_ROW[i][j]*=(-1);

			if(RANDOM_COLUMN[i][j]<0)
			RANDOM_COLUMN[i][j]*=(-1);
		}

	
	int x,y;
				for(int i=0;i<3;i++)
				{
					int total_no=(int)(TRANSITION[i]*100);	//epic mistake (int) is higher associative so it is actually
										// (int)TRANSITION   * 10;....CORRECTED BY (int)(transition*10)

				//	echo(""+total_no+" ---");

					while(total_no>0)
					{
					x=generate.nextInt()%10;
					x=x<0?~x:x;
					y=generate.nextInt()%10;
					y=y<0?~y:y;

						if(PROBABILITY_MATRIX[x][y]>-1 && i!=0)
						continue;
						
						if(i==0 && PROBABILITY_MATRIX[x][y]!=-1)
						continue;

						PROBABILITY_MATRIX[x][y]=i;
						total_no--;
					}
				}

		for(int i=0;i<10;i++)
		{
			for(int j=0;j<10;j++)
			echoc(""+PROBABILITY_MATRIX[i][j]+" ");
		
		echo("");
		}


	world=new double[world_size_rows][world_size_columns];
	utility=new double[world_size_rows][world_size_columns];
	error=new double[world_size_rows][world_size_columns];
	optimal_policy=new String[world_size_rows][world_size_columns];

	for(int i=0;i<world_size_rows;i++)
	for(int j=0;j<world_size_columns;j++)
	optimal_policy[i][j]=new String("null");


			for(int i=0;i<world_size_rows;i++)
			for(int j=0;j<world_size_columns;j++)
			world[i][j]=-0.04;

				world[1][1]=(INFINITY/SEED)*(-1);
				utility[1][1]=world[1][1];

			world[0][3]=1+INFINITY;
			utility[0][3]=1;

			world[1][3]=-1+INFINITY;
			utility[1][3]=-1;

		ALPHA=1;
		DISCOUNT=0.9;

	echo("Well those were a lot of questions...Here is the world that you entered");
	show_world();
	}
/*-------------------------------------------------------------------------------------------------------------------------------------------------------------*/

	void compute_policy()
	{
	double max=-99999;

		for(int i=0;i<3;i++)
		for(int j=0;j<4;j++)
		{
			if(world[i][j]>SEED || world[i][j]==(INFINITY/SEED)*(-1))
			continue;

		max=-99999;

			for(int action=0;action<=3;action++)
			{
				if(Q[action].q_value[i][j]>max)
				{
				max=Q[action].q_value[i][j];
				optimal_policy[i][j]=map(action);
				}
			}
		}
	}

	void display_q()
	{
	int i;
			for(i=UP;i<=RIGHT;i++)
			{
				echo("Q_VALUE FOR ACTION "+map(i));

				for(int x=0;x<world_size_rows;x++)
				{
					for(int y=0;y<world_size_columns;y++)
					echoc(""+Q[i].q_value[x][y]+"    ");
				echo("");
				}
			echo("");
			}
	}

	void display_n()
	{
	DecimalFormat d=new DecimalFormat("##.##");
	int i;
			for(i=UP;i<=RIGHT;i++)
			{
				echo("N_VALUE FOR ACTION "+map(i));

				for(int x=0;x<world_size_rows;x++)
				{
					for(int y=0;y<world_size_columns;y++)
					echoc(""+d.format(N[i].state[x][y])+"\t");
				echo("");
				}
			echo("");
			}
	}

/*-------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	void echo(String s)
	{
		System.out.println(s);
	}
/*-------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	void echoc(String s)
	{
		System.out.print(s);
	}
/*-------------------------------------------------------------------------------------------------------------------------------------------------------------*/
}
