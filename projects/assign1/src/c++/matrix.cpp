#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <papi.h>
#include <vector>
#include <chrono>
#include <fstream>

using namespace std;

#define SYSTEMTIME clock_t

 
std::vector<double> OnMult(int m_ar, int m_br) 
{
	
	chrono::high_resolution_clock::time_point Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;


	int EventSet = PAPI_NULL;
  	long long values[4];
  	int ret;
	

	ret = PAPI_library_init( PAPI_VER_CURRENT );
	if ( ret != PAPI_VER_CURRENT )
		std::cout << "FAIL" << endl;


	ret = PAPI_create_eventset(&EventSet);
		if (ret != PAPI_OK) std::cout << "ERROR: create eventset" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L1_DCM );
	if (ret != PAPI_OK) std::cout << "ERROR: PAPI_L1_DCM" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L2_DCM);
	if (ret != PAPI_OK) std::cout << "ERROR: PAPI_L2_DCM" << endl;
	
	ret = PAPI_add_event(EventSet,PAPI_TOT_CYC);
	if (ret != PAPI_OK) std::cout << "ERROR: PAPI_TOT_CYC" << endl;
	
	ret = PAPI_add_event(EventSet,PAPI_TOT_INS);
	if (ret != PAPI_OK) std::cout << "ERROR: PAPI_TOT_INS" << endl;
	
	
  pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
  phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;

	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);


	// Start counting
	ret = PAPI_start(EventSet);
	if (ret != PAPI_OK) std::cout << "ERROR: Start PAPI" << endl;

    Time1 = chrono::high_resolution_clock::now();

	for(i=0; i<m_ar; i++){	
		for( j=0; j<m_br; j++){	
			temp = 0;
			for( k=0; k<m_ar; k++){	
				temp += pha[i*m_ar+k] * phb[k*m_br+j];
			}
			phc[i*m_ar+j]=temp;
		}
	}


  Time2 = chrono::high_resolution_clock::now();

	ret = PAPI_stop(EventSet, values);
  		if (ret != PAPI_OK) std::cout << "ERROR: Stop PAPI" << endl;
  		printf("L1 DCM: %lld \n",values[0]);
  		printf("L2 DCM: %lld \n",values[1]);

		ret = PAPI_reset( EventSet );
		if ( ret != PAPI_OK )
			std::cout << "FAIL reset" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L1_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L2_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 
		
	ret = PAPI_remove_event( EventSet, PAPI_TOT_CYC );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 
		
	ret = PAPI_remove_event( EventSet, PAPI_TOT_INS );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_destroy_eventset( &EventSet );
	if ( ret != PAPI_OK )
		std::cout << "FAIL destroy" << endl;

	auto duration = chrono::duration_cast<chrono::nanoseconds>(Time2 - Time1);

	std::cout << "Time taken: " << (double) (duration.count() / 1e9) << " seconds" << std::endl;
	

	// display 10 elements of the result matrix tto verify correctness
	std::cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			std::cout << phc[j] << " ";
	}
	std::cout << endl;

    std::free(pha);
    std::free(phb);
    std::free(phc);

	return std::vector<double>({(double) (duration.count() / 1e9), (double)values[0], (double)values[1],(double)values[2], (double)values[3]});
	
	
}

// add code here for line x line matriz multiplication
std::vector<double> OnMultLine(int m_ar, int m_br)
{
    chrono::high_resolution_clock::time_point Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;


	int EventSet = PAPI_NULL;
  	long long values[4];
  	int ret;
	

	ret = PAPI_library_init( PAPI_VER_CURRENT );
	if ( ret != PAPI_VER_CURRENT )
		std::cout << "FAIL" << endl;


	ret = PAPI_create_eventset(&EventSet);
		if (ret != PAPI_OK) std::cout << "ERROR: create eventset" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L1_DCM );
	if (ret != PAPI_OK) std::cout << "ERROR: PAPI_L1_DCM" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L2_DCM);
	if (ret != PAPI_OK) std::cout << "ERROR: PAPI_L2_DCM" << endl;
	
	ret = PAPI_add_event(EventSet,PAPI_TOT_CYC);
	if (ret != PAPI_OK) std::cout << "ERROR: PAPI_TOT_CYC" << endl;
	
	ret = PAPI_add_event(EventSet,PAPI_TOT_INS);
	if (ret != PAPI_OK) std::cout << "ERROR: PAPI_TOT_INS" << endl;
	

		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;



	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);


	ret = PAPI_start(EventSet);
	if (ret != PAPI_OK) std::cout << "ERROR: Start PAPI" << endl;

    Time1 = chrono::high_resolution_clock::now();

	for(i=0; i<m_ar; i++)
	{	for( j=0; j<m_br; j++)
		{	temp = 0;
			for( k=0; k<m_ar; k++)
			{	
				phc[i * m_ar + k] += pha[i * m_ar + j] * phb[j * m_br + k];
			}
			
		}
	}

	Time2 = chrono::high_resolution_clock::now();

	ret = PAPI_stop(EventSet, values);
  		if (ret != PAPI_OK) std::cout << "ERROR: Stop PAPI" << endl;
  		printf("L1 DCM: %lld \n",values[0]);
  		printf("L2 DCM: %lld \n",values[1]);
  		printf("Cycles: %lld \n",values[2]);
  		printf("Instructions: %lld \n",values[3]);

		ret = PAPI_reset( EventSet );
		if ( ret != PAPI_OK )
			std::cout << "FAIL reset" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L1_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L2_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 
		
	ret = PAPI_remove_event( EventSet, PAPI_TOT_CYC );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 
		
	ret = PAPI_remove_event( EventSet, PAPI_TOT_INS );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_destroy_eventset( &EventSet );
	if ( ret != PAPI_OK )
		std::cout << "FAIL destroy" << endl;

    auto duration = chrono::duration_cast<chrono::nanoseconds>(Time2 - Time1);

	std::cout << "Time taken: " << (double) (duration.count() / 1e9) << " seconds" << std::endl;

	// display 10 elements of the result matrix tto verify correctness
	std::cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			std::cout << phc[j] << " ";
	}
	std::cout << endl;

    std::free(pha);
    std::free(phb);
    std::free(phc);
    
    return std::vector<double>({(double) (duration.count() / 1e9), (double)values[0], (double)values[1],(double)values[2], (double)values[3]});
    
    
}

// add code here for block x block matriz multiplication
std::vector<double> OnMultBlock(int m_ar, int m_br, int bkSize)
{
    
    chrono::high_resolution_clock::time_point Time1, Time2;
	
	char st[100];
	int i, ii, j, jj, k, kk;

	double *pha, *phb, *phc;
	

	int EventSet = PAPI_NULL;
  	long long values[4];
  	int ret;

	

	ret = PAPI_library_init( PAPI_VER_CURRENT );
	if ( ret != PAPI_VER_CURRENT )
		std::cout << "FAIL" << endl;

	


	ret = PAPI_create_eventset(&EventSet);
		if (ret != PAPI_OK) std::cout << "ERROR: create eventset" << endl;
	
	


	ret = PAPI_add_event(EventSet,PAPI_L1_DCM );
	if (ret != PAPI_OK) std::cout << "ERROR: PAPI_L1_DCM" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L2_DCM);
	if (ret != PAPI_OK) std::cout << "ERROR: PAPI_L2_DCM" << endl;
	
	ret = PAPI_add_event(EventSet,PAPI_TOT_CYC);
	if (ret != PAPI_OK) std::cout << "ERROR: PAPI_TOT_CYC" << endl;
	
	ret = PAPI_add_event(EventSet,PAPI_TOT_INS);
	if (ret != PAPI_OK) std::cout << "ERROR: PAPI_TOT_INS" << endl;
	
	
	
		
  pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;



	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);


	ret = PAPI_start(EventSet);
	if (ret != PAPI_OK) std::cout << "ERROR: Start PAPI" << endl;

    Time1 = chrono::high_resolution_clock::now();

	for (ii = 0 ; ii < m_ar ; ii += bkSize)
		for (jj = 0 ; jj < m_br ; jj += bkSize)
			for (kk = 0 ; kk < m_ar ; kk += bkSize)
				for (i = ii ; i < ii + bkSize ; i++)
					for (j = jj; j < jj + bkSize; j++)
						for (k = kk ; k < kk + bkSize ; k++)
							phc[i * m_ar + k] += pha[i * m_ar + j] * phb[j * m_br + k];


    Time2 = chrono::high_resolution_clock::now();

	ret = PAPI_stop(EventSet, values);
  		if (ret != PAPI_OK) std::cout << "ERROR: Stop PAPI" << endl;
  		printf("L1 DCM: %lld \n",values[0]);
  		printf("L2 DCM: %lld \n",values[1]);
  		printf("Cycles: %lld \n",values[2]);
  		printf("Instructions: %lld \n",values[3]);

		ret = PAPI_reset( EventSet );
		if ( ret != PAPI_OK )
			std::cout << "FAIL reset" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L1_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L2_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 
		
	ret = PAPI_remove_event( EventSet, PAPI_TOT_CYC );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 
		
	ret = PAPI_remove_event( EventSet, PAPI_TOT_INS );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_destroy_eventset( &EventSet );
	if ( ret != PAPI_OK )
		std::cout << "FAIL destroy" << endl;


	auto duration = chrono::duration_cast<chrono::nanoseconds>(Time2 - Time1);

	std::cout << "Time taken: " << (double) (duration.count() / 1e9) << " seconds" << std::endl;

	// display 10 elements of the result matrix tto verify correctness
	std::cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			std::cout << phc[j] << " ";
	}
	std::cout << endl;

    std::free(pha);
    std::free(phb);
    std::free(phc);
    
		
    return std::vector<double>({(double) (duration.count() / 1e9), (double)values[0], (double)values[1],(double)values[2], (double)values[3]});
    
}

void handle_error (int retval)
{
  printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
  exit(1);
}

void init_papi() {
  int retval = PAPI_library_init(PAPI_VER_CURRENT);
  if (retval != PAPI_VER_CURRENT && retval < 0) {
    printf("PAPI library version mismatch!\n");
    exit(1);
  }
  if (retval < 0) handle_error(retval);

  std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
            << " MINOR: " << PAPI_VERSION_MINOR(retval)
            << " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
}


int main (int argc, char *argv[])
{

	char c;
	int lin, col, blockSize;
	int op;
	int size;
	
	std::vector<double> result;
	std::ofstream file;
	


	op=1;
	do {
		std::cout << endl << "1. Multiplication" << endl;
		std::cout << "2. Line Multiplication" << endl;
		std::cout << "3. Block Multiplication" << endl;
		std::cout << "4. Line Multiplication (bigger matrixes)" << endl;
		std::cout << "Selection?: ";
		cin >> op;
		if (op == 0)
			break;

		switch (op){
			case 1: 

				//file.open("c++_dot_product_data.txt");
				file.open("c++_dot_product_instructions.txt");
				for (int size = 600; size <= 3000; size += 400) {
					result = OnMult(size, size);
					//file << size << ";" << result[0] << ";" << result[1] << ";" << result[2] << std::endl;
					file << size << ";"<< result[3] << ";" << result[4] << std::endl;
				}

				file.close();

				

				
				break;
			case 2:


				//file.open("c++_line_product_data.txt");
				file.open("c++_line_product_instructions.txt");
				for (int size = 600; size <= 3000; size += 400) {
					result = OnMultLine(size, size);  
					//file << size << ";" << result[0] << ";" << result[1] << ";" << result[2] << std::endl;
					file << size << ";"<< result[3] << ";" << result[4] << std::endl;
				}

				file.close();
				
				
				
				break;
			case 3:
				
				//file.open("c++_block_product_data.txt");  
				file.open("c++_block_product_instructions.txt");
				for (int size = 4096; size <= 10240; size += 2048) {
					for (int block = 128; block <= 1024; block *=2) {
						result = OnMultBlock(size, size, block);
						//file << size << ";" << block << ";" << result[0] << ";" << result[1] << ";" << result[2]  << std::endl;
						file << size << ";"<< result[3] << ";" << result[4] << std::endl;
					}
				}

				file.close();
				

				
				break;
			case 4:


				//file.open("c++_lineBigger_product_data.txt");
				file.open("c++_lineBigger_product_instructions.txt");
				for (int size = 4096; size <= 10240; size += 2048) {
					result = OnMultLine(size, size);  
					//file << size << ";" << result[0] << ";" << result[1] << ";" << result[2] << std::endl;
					file << size << ";"<< result[3] << ";" << result[4] << std::endl;
				}

				file.close();
				
				
				
				break;

		}

  		



	}while (op != 0);

	

}
