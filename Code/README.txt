copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA)
laboratory, Boston University. All rights reserved. Permission to use, copy, modify, and 
distribute this software and its documentation for any purpose and without fee is hereby 
granted, provided that the above copyright notice appear in all copies and that both the 
copyright notice and this permission notice appear in  supporting documentation. The RINA 
laboratory of the Computer Science Department at Boston University makes no representations 
about the suitability of this software for any purpose. It is provided "as is" without express 
or implied warranty. 

author Yuefeng Wang. Computer Science Department, Boston University 

====================================================================

(1) All source codes are under the ¡°src¡± directory. Simple demos¡¯ source codes are included 
in this directory. See the configuration files in ¡°experimentConfigFiles¡± directory for details 
about demos¡¯ setup. 

   (A) ¡°a_simple_demo¡±: There are two RINA nodes, and each has one application process and one 
IPC presses. Node 2¡¯ s application process ¡°dummyApp2¡± allocates a flow to Node 1¡¯s application 
process ¡°dummyApp1¡± using the underlying DIF¡¯s communication services, and the DIF has two IPC 
processes. Node 1¡¯s IPC process ¡°BostonU1¡± is the first member in the DIF, and Node 2¡¯s IPC 
process ¡°BostonU2¡± is enrolled by ¡±BostonU1¡±.

   (B) ¡°a_routing_demo¡±: There are five RINA nodes, and each has one IPC process. All those five 
IPC processes are members of the same DIF. This demo shows how RINA routing works inside the 
DIF. This is the DIF setup used in the RINA video application example in the manual. See the 
manual for more details.  

   (C) ¡°a_ddf_demo¡±: Thera are three RINA nodes, and each has one application process. Node 1 
and Node 3 have one IPC process on it, and Node 2 have two IPC processes on it. Node 1¡¯s 
application process ¡°app1¡± wants to allocate a flow to Node 3¡¯s application process ¡°app3¡±, 
however there are no underlying DIF could provide the communication service. Then a DIF is 
dynamically created by Node 2¡¯s application process ¡°app2¡± which is registered to provided the 
relay service to ¡°app3¡±. The newly created DIF will provided the communication service between 
¡°app1¡± and ¡°app3¡±. See dynamic DIF formation in the manual for details. 
 
   (D) ¡°a¡±_video_demo: There are two RINA nodes, and each has one application process and one 
IPC process. One has a RINA video streaming server proxy and the other has a RINA video 
steaming client proxy. Server proxy is connected to client proxy using the communication service 
provided by the underlying DIF. Once two proxies are connected, user can use a video 
application, such as VLC player to retrieve the video served on live555 video server 
application. See RINA video streaming application in the manual for details. 

	
   (E) ¡°a_mobility_demo¡±: There are two RINA nodes. Node 2 has two IPC processes belonging to two
DIFs. Node 1 is a moving node, at the beginning it uses one IPC in the first DIF to talk to Node 2.
Then Node 1 moves to another location, then uses another IPC process of another DIF to talk to Node
2. This demo shows the simple case for mobility in RINA. 


(2) All included libraries are under the ¡°lib¡± directory. 

(3) All proto files are under the ¡°protoFiles¡± directory.

(4) All RINA configuration files used in RINA demos are under ¡°experimentConfigFiles¡± directory. 

(5) The ¡°streamingServer¡± directory includes a live555 media server and one small video file.  
These are used for RINA video streaming application demo. 