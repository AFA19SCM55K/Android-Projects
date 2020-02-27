#include<stdio.h>
#include<stdlib.h>
#include "buffer_mgr.h"
#include "storage_mgr.h"
#include <math.h>

// This structure represents one page frm in buffer pool (memory).
//Group 2 ADO
typedef struct Page
{
	SM_PageHandle data; // Actual data of the page
	PageNumber pageNum; // An identification integer given to each page
	int dirtyBit; // Used to indicate whether the contents of the page has been modified by the client
	int fixCount; // Used to indicate the number of clients using that page at a given instance
	int hitNum;   // Used by LRU algorithm to get the least recently used page	
	int refNum;   // Used by LFU algorithm to get the least frequently used page
	int pinStatus;
} Frame;

// "size_of_buffer" represents the size of the buffer pool i.e. maximum number of page frames that can be kept into the buffer pool
int size_of_buffer = 0;

// "index_of_rear" basically stores the count of number of pages read from the disk.
// "index_of_rear" is also used by FIFO function to calculate the frontIndex i.e.
int index_of_rear = 0;

// "countWrite" counts the number of I/O write to the disk i.e. number of pages writen to the disk
int countWrite = 0;

// "hit" a general count which is incremented whenever a page frm is added into the buffer pool.
// "hit" is used by LRU to determine least recently added page into the buffer pool.
int hit = 0;

// "pointer_of_clock" is used by CLOCK algorithm to point to the last added page in the buffer pool.
int pointer_of_clock = 0;

// "pointer_lfu" is used by LFU algorithm to store the least frequently used page frm's position. It speeds up operation  from 2nd replacement onwards.
int pointer_lfu = 0;

int maxBufferSize;

// Defining FIFO (First In First Out) function
extern void FIFO(BM_BufferPool *const bm, Frame *page)
{
	//printf("FIFO Started");
	Frame *frm = (Frame *) bm->mgmtData;

	int i=0, frontIndex;
	frontIndex = index_of_rear % size_of_buffer;

	// Interating through all the page frames in the buffer pool
	while(i < size_of_buffer)
	{
		if(frm[frontIndex].fixCount != 0)
		{
			
			// If the current page frm is being used by some client, we move on to the next location
			frontIndex++;
			// frontIndex = (frontIndex % size_of_buffer == 0) ? 0 : frontIndex;

			switch (frontIndex % size_of_buffer)
			{
			case 0:
				frontIndex = 0;
				break;

			default:
				frontIndex = frontIndex;
				break;
			}
		}
		else
		{
			// If page in memory has been modified (dirtyBit = 1), then write page to disk
			if(frm[frontIndex].dirtyBit == 1)
			{
				SM_FileHandle fh;
				openPageFile(bm->pageFile, &fh);
				writeBlock(frm[frontIndex].pageNum, &fh, frm[frontIndex].data);
				
				// Increase the countWrite which records the number of writes done by the buffer manager.
				countWrite++;
			}
			
			// Setting page frm's content to new page's content
			frm[frontIndex].data = page->data;
			frm[frontIndex].pageNum = page->pageNum;
			frm[frontIndex].dirtyBit = page->dirtyBit;
			frm[frontIndex].fixCount = page->fixCount;
			break;
		}
		i++;
	}
}

// Defining LFU (Least Frequently Used) function
extern void LFU(BM_BufferPool *const bm, Frame *page)
{
	//printf("LFU Started");
	Frame *frm = (Frame *) bm->mgmtData;
	
	int i=0, j=0, leastFreqIndex, leastFreqRef;
	leastFreqIndex = pointer_lfu;	
	
	// Interating through all the page frames in the buffer pool
	while(i < size_of_buffer)
	{
		if(frm[leastFreqIndex].fixCount == 0)
		{
			leastFreqIndex = (leastFreqIndex + i) % size_of_buffer;
			leastFreqRef = frm[leastFreqIndex].refNum;
			break;
		}
		i++;
	}

	i = (leastFreqIndex + 1) % size_of_buffer;

	// Finding the page frm having minimum refNum (i.e. it is used the least frequent) page frm
	while(j < size_of_buffer)
	{
		if(frm[i].refNum < leastFreqRef)
		{
			leastFreqIndex = i;
			leastFreqRef = frm[i].refNum;
		}
		i = (i + 1) % size_of_buffer;
		j++;
	}
		
	// If page in memory has been modified (dirtyBit = 1), then write page to disk	
	if(frm[leastFreqIndex].dirtyBit == 1)
	{
		SM_FileHandle fh;
		openPageFile(bm->pageFile, &fh);
		writeBlock(frm[leastFreqIndex].pageNum, &fh, frm[leastFreqIndex].data);
		
		// Increase the countWrite which records the number of writes done by the buffer manager.
		countWrite++;
	}
	
	// Setting page frm's content to new page's content		
	frm[leastFreqIndex].data = page->data;
	frm[leastFreqIndex].pageNum = page->pageNum;
	frm[leastFreqIndex].dirtyBit = page->dirtyBit;
	frm[leastFreqIndex].fixCount = page->fixCount;
	pointer_lfu = leastFreqIndex + 1;
}

// Defining LRU (Least Recently Used) function
extern void LRU(BM_BufferPool *const bm, Frame *page)
{	
	Frame *frm = (Frame *) bm->mgmtData;
	int i=0, leastHitIndex, leastHitNum;

	// Interating through all the page frames in the buffer pool.
	while(i < size_of_buffer)
	{
		// Finding page frm whose fixCount = 0 i.e. no client is using that page frm.
		if(frm[i].fixCount == 0)
		{
			leastHitIndex = i;
			leastHitNum = frm[i].hitNum;
			break;
		}
		i++;
	}	

	// Finding the page frm having minimum hitNum (i.e. it is the least recently used) page frm
	i = leastHitIndex;
	while(i < size_of_buffer)
	{
		if(frm[i].hitNum < leastHitNum)
		{
			leastHitIndex = i;
			leastHitNum = frm[i].hitNum;
		}
		i++;
	}

	// If page in memory has been modified (dirtyBit = 1), then write page to disk
	if(frm[leastHitIndex].dirtyBit == 1)
	{
		SM_FileHandle fh;
		openPageFile(bm->pageFile, &fh);
		writeBlock(frm[leastHitIndex].pageNum, &fh, frm[leastHitIndex].data);
		
		// Increase the countWrite which records the number of writes done by the buffer manager.
		countWrite++;
	}
	
	// Setting page frm's content to new page's content
	frm[leastHitIndex].data = page->data;
	frm[leastHitIndex].pageNum = page->pageNum;
	frm[leastHitIndex].dirtyBit = page->dirtyBit;
	frm[leastHitIndex].fixCount = page->fixCount;
	frm[leastHitIndex].hitNum = page->hitNum;
}

// Defining CLOCK function
extern void CLOCK(BM_BufferPool *const bm, Frame *page)
{	
	//printf("CLOCK Started");
	Frame *frm = (Frame *) bm->mgmtData;
	while(1)
	{

		if(pointer_of_clock % size_of_buffer == 0)
		{
			pointer_of_clock = 0;
		}
		else
		{
			pointer_of_clock = pointer_of_clock;
		}
		if(frm[pointer_of_clock].hitNum != 0)
		{
			// // Incrementing pointer_of_clock so that we can check the next page frm location.
			// // We set hitNum = 0 so that this loop doesn't go into an infinite loop.
			frm[pointer_of_clock++].hitNum = 0;	
		}
		else
		{
				

			// If page in memory has been modified (dirtyBit = 1), then write page to disk
			if(frm[pointer_of_clock].dirtyBit == 1)
			{
				SM_FileHandle fh;
				openPageFile(bm->pageFile, &fh);
				writeBlock(frm[pointer_of_clock].pageNum, &fh, frm[pointer_of_clock].data);
				
				// Increase the countWrite which records the number of writes done by the buffer manager.
				countWrite++;
			}
			
			// Setting page frm's content to new page's content
			frm[pointer_of_clock].data = page->data;
			frm[pointer_of_clock].pageNum = page->pageNum;
			frm[pointer_of_clock].dirtyBit = page->dirtyBit;
			frm[pointer_of_clock].fixCount = page->fixCount;
			frm[pointer_of_clock].hitNum = page->hitNum;
			pointer_of_clock++;
			break;	
		}
	}
}

// ***** BUFFER POOL FUNCTIONS ***** //

/* 
   This function creates and initializes a buffer pool with numPages page frames.
   pageFileName stores the name of the page file whose pages are being cached in memory.
   strategy represents the page replacement strategy (FIFO, LRU, LFU, CLOCK) that will be used by this buffer pool
   stratData is used to pass parameters if any to the page replacement strategy
*/
extern RC initBufferPool(BM_BufferPool *const bm, const char *const pageFileName, 
		  const int numPages, ReplacementStrategy strategy, 
		  void *stratData)
{
	maxBufferSize = numPages;
	FILE *f = fopen(pageFileName, "r+");
	if(f!=NULL)
	{
	bm->pageFile = (char *)pageFileName;
	bm->numPages = numPages;
	bm->strategy = strategy;

	Frame *page = malloc(sizeof(Frame) * numPages);
	
	size_of_buffer = numPages;	
	int i=0;
	while(i < size_of_buffer)
	{
		page[i].data = NULL;
		page[i].pinStatus = 0;
		page[i].pageNum = -1;
		page[i].dirtyBit = 0;
		page[i].fixCount = 0;
		page[i].hitNum = 0;	
		page[i].refNum = 0;
		i++;
	}

	bm->mgmtData = page;
	countWrite = pointer_of_clock = pointer_lfu = 0;
	return RC_OK;
	}
	else
	{
		RC_FILE_NOT_FOUND;
	}
	
		
}

// Shutdown i.e. close the buffer pool, thereby removing all the pages from the memory and freeing up all resources and releasing some memory space.
extern RC shutdownBufferPool(BM_BufferPool *const bm)
{
	Frame *frm = (Frame *)bm->mgmtData;
	// Write all dirty pages (modified pages) back to disk
	forceFlushPool(bm);

	int i;	
	for(i = 0; i < size_of_buffer; i++)
	{
		// If fixCount != 0, it means that the contents of the page was modified by some client and has not been written back to disk.
		if(frm[i].fixCount != 0)
		{
			return RC_PINNED_PAGES_IN_BUFFER;
		}
	}

	// Releasing space occupied by the page
	free(frm);
	bm->mgmtData = NULL;
	return RC_OK;
}

// This function writes all the dirty pages (having fixCount = 0) to disk
// dirty pages == buffer madhe write zalet pan disc madhe nai zalet
extern RC forceFlushPool(BM_BufferPool *const bm)
{
	Frame *frm = (Frame *)bm->mgmtData;
	
	int i=0;
	// Store all dirty pages (modified pages) in memory to page file on disk	
	while(i < size_of_buffer)
	{
		if(frm[i].dirtyBit == 1)
		{
			if(frm[i].fixCount == 0){
			SM_FileHandle fh;
			// Opening page file available on disk
			openPageFile(bm->pageFile, &fh);
			// Writing block of data to the page file on disk
			writeBlock(frm[i].pageNum, &fh, frm[i].data);
			// Mark the page not dirty.
			frm[i].dirtyBit = 0;
			// Increase the countWrite which records the number of writes done by the buffer manager.
			countWrite++;
		}
	}
		i++;
	}	
	return RC_OK;
}


// ***** PAGE MANAGEMENT FUNCTIONS ***** //

// This function marks the page as dirty indicating that the data of the page has been modified by the client
extern RC markDirty (BM_BufferPool *const bm, BM_PageHandle *const page)
{
	Frame *frm = (Frame *)bm->mgmtData;
	
	int x=0;
	// Iterating through all the pages in the buffer pool
	while(x < size_of_buffer)
	{
		// If the current page is the page to be marked dirty, then set dirtyBit = 1 (page has been modified) for that page
		if(frm[x].pageNum == page->pageNum)
		{
			frm[x].dirtyBit = 1;
			return RC_OK;
		}
		x++;			
	}		
	return RC_ERROR;
}

// This function unpins a page from the memory i.e. removes a page from the memory
extern RC unpinPage (BM_BufferPool *const bm, BM_PageHandle *const page)
{	
	printf("Updated unpinPage success...");
	Frame *frm = (Frame *)bm->mgmtData;
	
	int i=0;
	// Iterating through all the pages in the buffer pool
	while(i < size_of_buffer)
	{
		// If the current page is the page to be unpinned, then decrease fixCount (which means client has completed work on that page) and exit loop
		if(page->pageNum==frm[i].pageNum)
		{
			frm[i].pinStatus=0;
			if(frm[i].fixCount>0){
			frm[i].fixCount--;
			}
			else
			{
				frm[i].fixCount=0;
			}
			frm[1].pinStatus=0;
			break;		
		}
		i++;		
	}
	return RC_OK;
}

// This function writes the contents of the modified pages back to the page file on disk
extern RC forcePage (BM_BufferPool *const bm, BM_PageHandle *const page)
{
	Frame *frm = (Frame *)bm->mgmtData;
	
	int i=0;
	// Iterating through all the pages in the buffer pool
	while(i < size_of_buffer)
	{
		// If the current page = page to be written to disk, then right the page to the disk using the storage manager functions
		if(frm[i].pageNum == page->pageNum)
		{		
			SM_FileHandle fh;
			openPageFile(bm->pageFile, &fh);
			writeBlock(frm[i].pageNum, &fh, frm[i].data);
		
			// Mark page as undirty because the modified page has been written to disk
			frm[i].dirtyBit = 0;
			
			// Increase the countWrite which records the number of writes done by the buffer manager.
			countWrite++;
		}
		i++;
	}	
	return RC_OK;
}

// This function pins a page with page number pageNum i.e. adds the page with page number pageNum to the buffer pool.
// If the buffer pool is full, then it uses appropriate page replacement strategy to replace a page in memory with the new page being pinned. 
extern RC pinPage (BM_BufferPool *const bm, BM_PageHandle *const page, 
	    const PageNumber pageNum)
{
	Frame *frm = (Frame *)bm->mgmtData;
	
	// Checking if buffer pool is empty and this is the first page to be pinned
	if(frm[0].pageNum == -1)
	{
		// Reading page from disk and initializing page frm's content in the buffer pool
		SM_FileHandle fh;
		openPageFile(bm->pageFile, &fh);
		frm[0].data = (SM_PageHandle) malloc(PAGE_SIZE);
		ensureCapacity(pageNum,&fh);
		readBlock(pageNum, &fh, frm[0].data);
		frm[0].pageNum = pageNum;
		frm[0].fixCount++;
		index_of_rear = hit = 0;
		frm[0].hitNum = hit;	
		frm[0].refNum = 0;
		page->pageNum = pageNum;
		page->data = frm[0].data;
		
		return RC_OK;		
	}
	else
	{	
		int i=0;
		bool buffer_is_full = true;
		while(i < size_of_buffer)
		{
			if(frm[i].pageNum == -1)
			{	
				SM_FileHandle fh;
				openPageFile(bm->pageFile, &fh);
				frm[i].data = (SM_PageHandle) malloc(PAGE_SIZE);
				readBlock(pageNum, &fh, frm[i].data);
				frm[i].pageNum = pageNum;
				frm[i].fixCount = 1;
				frm[i].refNum = 0;
				index_of_rear++;	
				hit++; // Incrementing hit (hit is used by LRU algorithm to determine the least recently used page)

				switch (bm->strategy)
				{
				case RS_LRU:
					frm[i].hitNum = hit;
					break;
				case RS_CLOCK:
					frm[i].hitNum = 1;
					break;
				default:
					break;
				}



				// if(bm->strategy == RS_LRU)
				// 	// LRU algorithm uses the value of hit to determine the least recently used page
				// 	frm[i].hitNum = hit;				
				// else if(bm->strategy == RS_CLOCK)
				// 	// hitNum = 1 to indicate that this was the last page frm examined (added to the buffer pool)
				// 	frm[i].hitNum = 1;
						
				page->pageNum = pageNum;
				page->data = frm[i].data;
				
				buffer_is_full = false;
				break;
								
			} else {
				
				// Checking if page is in memory
				if(frm[i].pageNum == pageNum)
				{
					// Increasing fixCount i.e. now there is one more client accessing this page
					frm[i].fixCount++;
					buffer_is_full = false;
					hit++; // Incrementing hit (hit is used by LRU algorithm to determine the least recently used page)
					switch (bm->strategy)
					{
					case RS_LRU:
							frm[i].hitNum = hit;
							break;
					case RS_CLOCK:
							frm[i].hitNum = 1;
							break;
					case RS_LFU:
							frm[i].refNum++;
							break;
					default:
						break;
					}
					
					page->pageNum = pageNum;
					page->data = frm[i].data;
					pointer_of_clock++;
					break;
				}
			}
			i++;
		}
		
		// If buffer_is_full = true, then it means that the buffer is full and we must replace an existing page using page replacement strategy
		if(buffer_is_full == true)
		{
			// Create a new page to store data read from the file.
			Frame *newPage = (Frame *) malloc(sizeof(Frame));		
			
			// Reading page from disk and initializing page frm's content in the buffer pool
			SM_FileHandle fh;
			openPageFile(bm->pageFile, &fh);
			newPage->data = (SM_PageHandle) malloc(PAGE_SIZE);
			readBlock(pageNum, &fh, newPage->data);
			newPage->pageNum = pageNum;
			newPage->dirtyBit = 0;		
			newPage->fixCount = 1;
			newPage->refNum = 0;
			index_of_rear++;
			hit++;

			switch (bm->strategy)
			{
			case RS_LRU:
				newPage->hitNum = hit;
				break;
			case RS_CLOCK:
				newPage->hitNum = 1;
				break;

			default:
				break;
			}

			page->pageNum = pageNum;
			page->data = newPage->data;			

			// Call appropriate algorithm's function depending on the page replacement strategy selected (passed through parameters)
			if(bm->strategy == RS_FIFO)
			{
				FIFO(bm, newPage);
			}
			else if(bm->strategy == RS_LRU)
			{
				LRU(bm, newPage);
			}
			else if(bm->strategy == RS_CLOCK)
			{
				CLOCK(bm, newPage);
			}
			else if(bm->strategy == RS_LFU)
			{
				LFU(bm, newPage);
			}
			else if(bm->strategy == RS_LRU_K)
			{
				printf("\n LRU-k algorithm not implemented");
			}
			else
			{
				printf("\nAlgorithm Not Implemented\n");
			}			
		}		
		return RC_OK;
	}	
}


// ***** STATISTICS FUNCTIONS ***** //

// This function returns an array of page numbers.
extern PageNumber *getFrameContents (BM_BufferPool *const bm)
{
	PageNumber *frame_contents = malloc(sizeof(PageNumber) * size_of_buffer);
	Frame *frm = (Frame *) bm->mgmtData;
	
	
	// Iterating through all the pages in the buffer pool and setting frame_contents' value to pageNum of the page
	for(int i=0;i < size_of_buffer;i++) {
		frame_contents[i] = (frm[i].pageNum != -1) ? frm[i].pageNum : NO_PAGE;
	}
	return frame_contents;
	free(frame_contents);
}

// This function returns an array of bools, each element represents the dirtyBit of the respective page.
extern bool *getDirtyFlags (BM_BufferPool *const bm)
{
	bool *dirty_flags = malloc(sizeof(bool) * size_of_buffer);
	Frame *frm = (Frame *)bm->mgmtData;
	
	int i=0;
	// Iterating through all the pages in the buffer pool and setting dirty_flags' value to TRUE if page is dirty else FALSE
	while(i < size_of_buffer)
	{
		// dirty_flags[i] = (frm[i].dirtyBit == 1) ? true : false ;
		if(frm[i].dirtyBit == 1)
		{
			dirty_flags[i]=true;
		}
		else
		{
			dirty_flags[i]=false;
		}
		
		i++;
	}	
	return dirty_flags;
	free(dirty_flags);
}

// This function returns an array of ints (of size numPages) where the ith element is the fix count of the page stored in the ith page frm.
extern int *getFixCounts (BM_BufferPool *const bm)
{
	int *fixCounts = malloc(sizeof(int) * size_of_buffer);
	Frame *frm= (Frame *)bm->mgmtData;
	// Iterating through all the pages in the buffer pool and setting fixCounts' value to page's fixCount
	for(int i=0;i < size_of_buffer;i++)
	{
		if(frm[i].fixCount != -1){
			fixCounts[i]=frm[i].fixCount;
		}
		else
		{
			fixCounts[i]=0;
		}
	}	
	return fixCounts;
	free(fixCounts);
}

// This function returns the number of pages that have been read from disk since a buffer pool has been initialized.
extern int getNumReadIO (BM_BufferPool *const bm)
{
	// Adding one because with start index_of_rear with 0.
	return (index_of_rear + 1);
}

// This function returns the number of pages written to the page file since the buffer pool has been initialized.
extern int getNumWriteIO (BM_BufferPool *const bm)
{
	return countWrite;
}