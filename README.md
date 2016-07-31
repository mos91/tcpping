[![Build Status](https://travis-ci.org/mos91/tcpping.svg?branch=master)](https://travis-ci.org/mos91/tcpping)

TCPPing is simple network tool for estimating and gathering network bandwith metrics with remote host, on which the same instance of this tool was started.

The metrics are gathered from connection:
* Messages per second, sent over network (Msg/s)
* Average time in the last second needed for the message to complete a cycle (A->B->A) (Avg.RTT)
* Total maximum time needed for the message to complete a cycle (A->B->A) (Max.RTT)
* Average time for the previous second needed for the message to come from A to B (A->B) (Avg.SendTime)
* Average time for the previous second needed for the message to come from B to A (B->A) (Avg.RcvTime)
* Total count of lost message (TotalLoss)
* Value of lost messages in previous second (Loss/s)

Usage :
+ Run the tool on CompA as a 'Catcher' first
`tcpping -c -bind 192.168.0.101 -port 9900`
+ Then run the pitcher instance, simply putting the following text in command line
`tcpping -p -size 50 -mps 10 -port 9900`
