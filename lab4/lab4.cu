#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include <time.h>
#include <cuda_runtime.h>

#define BLOCK_SIZE 32

#define WA (10 * BLOCK_SIZE) // Matrix A width
#define HA (10 * BLOCK_SIZE) // Matrix A height
#define WB (20 * BLOCK_SIZE) // Matrix B width
#define HB WA  // Matrix B height
#define WC WB  // Matrix C width 
#define HC HA  // Matrix C height

void MatInit(float* data, int size)
{
    for (int i = 0; i < size; ++i)
        data[i] = rand() / (float)RAND_MAX;
}

__device__  float * GetSubMatrix(float *matrix, int m, int index, int width)
{
	return  matrix+width*BLOCK_SIZE*index+BLOCK_SIZE*m;
}

void CpuMul(float* C, const float* A, const float* B, int hA, int wA, int wB)
{
    for (int i = 0; i < hA; ++i)
        for (int j = 0; j < wB; ++j)
		{
            double sum = 0;
            for (int k = 0; k < wA; ++k) 
			{
                double a = A[i * wA + k];
                double b = B[k * wB + j];
                sum += a * b;
            }
            C[i * wB + j] = (float)sum;
        }
}

__global__ void GpuMul1( float* C, float* A, float* B, int wA, int wB)
{
    int bx = blockIdx.x;
    int tx = threadIdx.x;    

	int idx = bx * blockDim.x + tx;
	int row = idx / wB;
	int column = idx % wB;

	float sum = 0;

	for(int i = 0; i < wA; ++i)
	{
		sum += A[row * wA + i] * B[i * wB + column];
	}
	C[row * wB + column] = sum;
//	printf("%d %d %d %d %f\n",row * wB + column,idx,row,column,sum);
}

__global__ void GpuMul2( float* C, float* A, float* B, int wA, int wB)
{
    __shared__ float As[BLOCK_SIZE][BLOCK_SIZE];

    __shared__ float Bs[BLOCK_SIZE][BLOCK_SIZE];

    int bx = blockIdx.x;
    int by = blockIdx.y;

    int tx = threadIdx.x;
    int ty = threadIdx.y;     

    float sum = 0;

    for (int m= 0; m<wA/BLOCK_SIZE; m++) 
	{
		float *subA=GetSubMatrix(A, m, by, wA);

		float *subB=GetSubMatrix(B, bx, m, wB);

        As[ty][tx] = *(subA+ wA * ty + tx);
        Bs[ty][tx] = *(subB+ wB * ty + tx);

        __syncthreads();
        for (int k = 0; k < BLOCK_SIZE; ++k)
            sum += As[ty][k] * Bs[k][tx];
        __syncthreads();
    }

	float *subC=GetSubMatrix(C, bx, by, wB);
    *(subC + wB * ty + tx)= sum;
}

int main()
{
    // allocate host memory for matrices A and B
    int size_A = WA * HA;
    int mem_size_A = sizeof(float) * size_A;
    float* h_A = (float*) malloc(mem_size_A);

    int size_B = WB * HB;
    int mem_size_B = sizeof(float) * size_B;
    float* h_B = (float*) malloc(mem_size_B);
	clock_t start, finish;
	double time[3];

    MatInit(h_A, size_A);
    MatInit(h_B, size_B);

    float* d_A;
    cudaMalloc((void**) &d_A, mem_size_A);
    float* d_B;
    cudaMalloc((void**) &d_B, mem_size_B);

    // copy host memory to device
    cudaMemcpy(d_A, h_A, mem_size_A, cudaMemcpyHostToDevice) ;
    cudaMemcpy(d_B, h_B, mem_size_B, cudaMemcpyHostToDevice) ;

    // allocate device memory for result
    int size_C = WC * HC;
    int mem_size_C = sizeof(float) * size_C;
    float* d1_C;
    cudaMalloc((void**) &d1_C, mem_size_C);
	float* d2_C;
    cudaMalloc((void**) &d2_C, mem_size_C);

    // allocate host memory for the result
	float* h0_C = (float*) malloc(mem_size_C);
    float* h1_C = (float*) malloc(mem_size_C);
	float* h2_C = (float*) malloc(mem_size_C);
    
	start=clock();
	int threads1 = BLOCK_SIZE * BLOCK_SIZE;
	int grid1 = WC*HC/threads1;
    GpuMul1<<< grid1, threads1 >>>(d1_C, d_A, d_B, WA, WB);
	cudaThreadSynchronize();
	finish=clock();  
	time[1]=(double)(finish-start)/CLOCKS_PER_SEC;
    cudaMemcpy(h1_C, d1_C, mem_size_C, cudaMemcpyDeviceToHost);

	start=clock();
	dim3 threads2(BLOCK_SIZE, BLOCK_SIZE);
	dim3 grid2(WC / threads2.x, HC / threads2.y);
    GpuMul2<<< grid2, threads2 >>>(d2_C, d_A, d_B, WA, WB);
	cudaThreadSynchronize();
	finish=clock();  
	time[2]=(double)(finish-start)/CLOCKS_PER_SEC;
    cudaMemcpy(h2_C, d2_C, mem_size_C, cudaMemcpyDeviceToHost);

	start=clock();
    CpuMul(h0_C, h_A, h_B, HA, WA, WB);
	finish=clock();
	time[0]=(double)(finish-start)/CLOCKS_PER_SEC;

//	for(int i=0;i<WC*HC;i++)
//		printf("%f %f %f\n",h0_C[i],h1_C[i],h2_C[i]);

	printf("%f  %f  %f",time[0],time[1],time[2]);

    // clean up memory
    free(h_A);
    free(h_B);
    free(h0_C);
	free(h1_C);
	free(h2_C);
    cudaFree(d_A);
    cudaFree(d_B);
    cudaFree(d1_C);
	cudaFree(d2_C);
    cudaThreadExit();
}

 