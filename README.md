# SFU RecQ: Drop-In Sports Facility Availability and On-Site Queuing System

**Team members:** Emily Tsou, Jiawen Wang, Dmitriy Petrichshev, Elysia Anne Wimmer, and Zhengyang Gu  
**Institution:** School of Computing Science, Simon Fraser University  
**Course:** CMPT 276: Introduction to Software Engineering  
**Instructor:** Bobby Chan  
**Date:** October 19, 2026

## Project Information

- **Group:** [Add group number]
- **GitHub repository:** [Add repository URL]
- **Live application on Render:** [Add Render URL]

## Abstract

Simon Fraser University (SFU) provides drop-in sports sessions with published schedules showing activities, times, and locations. However, these schedules do not show the current availability of individual courts or the number of people waiting. Participants may arrive at a gym only to find all courts occupied, without knowing who is waiting or whose turn should come next. As a result, they may spend unnecessary time watching for available courts or risk missing an opportunity if they leave temporarily. To address these problems, our group proposes SFU RecQ, a browser-based application that displays individual court availability and manages on-site waiting queues. Recreation staff will update court statuses through an administrator interface, allowing participants to check availability and waiting counts before visiting. To join a queue, participants must first arrive at the gym and complete an on-site check-in with staff. Staff will then register waiting participants in a first-in, first-out (FIFO) queue based on their arrival order. Once registered, participants can view the current queue status and their own position through the application. When a court becomes available, the next participant will receive an email notification and have three minutes to return for staff confirmation. If the deadline is missed, the opportunity will automatically pass to the next person in line. SFU RecQ aims to reduce unnecessary trips, clarify waiting order, and make drop-in sports more organized, transparent, and fair.

## Project Introduction

SFU provides a drop-in sports schedule that lists activities such as badminton, basketball, volleyball, and pickleball, along with their scheduled times and locations. Although this schedule helps participants decide when and where to play, it does not show the current availability of individual courts or playing areas, or the number of people waiting. As a result, participants may arrive at a gym only to find all spaces occupied, with no clear way to determine who has been waiting longer or whose turn should come next. To address these problems, our group proposes SFU RecQ, a browser-based application that displays staff-updated availability for individual courts and playing areas, along with current waiting counts. Participants must check in with Recreation staff on-site to join a first-in, first-out (FIFO) waiting queue. Once registered, they can view their queue position online and receive an email notification when it is their turn. The system is designed to support different SFU drop-in sports activities where individual playing spaces and waiting queues can be managed.

## Problem Statement and Target Audience

### User Needs and Current Challenges

One of the main challenges for SFU drop-in sports participants is the lack of information about court availability and waiting conditions. Although participants can check when and where activities take place, they cannot easily determine how many courts are currently occupied or how many people are waiting to use them. Based on our experience with drop-in badminton, when all courts are occupied, participants generally wait on-site without a clearly organized queue. This creates uncertainty because they may not know who arrived first, whose turn should come next, or when a court will become available.

This situation can be especially inconvenient for students who need to plan their time around classes and other responsibilities. Staying at the gym to wait for an available court can take up valuable time, while leaving temporarily may mean losing the opportunity to play. As a result, participants need a more reliable way to check individual court availability and see how many people are waiting before visiting the gym. They also need a clear waiting order and a way to track their own queue position after checking in on-site, without having to constantly watch for an available court.

### Target Users and Expected Benefits

The target users of SFU RecQ can be divided into two main groups: eligible drop-in sports participants and SFU Recreation staff. The first group includes students, staff, and other individuals who are eligible to use SFU's drop-in sports facilities. These participants need a convenient way to check the availability of individual courts and understand current waiting conditions. Through SFU RecQ, they will be able to view court availability and the number of people waiting before visiting the gym. After completing an on-site check-in with Recreation staff, participants can join a waiting queue and track their position through the application. They will also receive email notifications when it is their turn to use a court. This allows participants to plan their visits more effectively and wait without having to constantly watch for an available court.

The second group consists of SFU Recreation staff, who are responsible for managing the facilities and assisting participants. Through an administrator interface, staff will be able to update court availability, register participants in the on-site waiting queue after verifying their arrival, monitor queue status, confirm turns, and handle exceptional situations. The system will automatically manage the waiting order based on a first-in, first-out (FIFO) principle, preventing arbitrary changes to the normal queue order. By providing clearer information and a more organized waiting process, SFU RecQ aims to improve the experience of participants while helping Recreation staff manage facility usage more efficiently.

## Existing Systems and Competitive Analysis

### Current Ways of Addressing the Problem

Currently, SFU provides an online Drop-In Sports Schedule that allows participants to check sports activities, scheduled times, and gym locations. This information helps participants plan when and where to visit, but it does not indicate whether individual courts or playing areas are currently occupied or how many people are waiting. Therefore, participants still need to visit the gym in person to find out if there is an available space.

Based on our experience with drop-in badminton, participants usually need to enter the gym and visually check each court to see whether it is occupied. During our visits, we did not observe a visible system showing court availability or a clearly organized waiting queue. Recreation staff mainly handled participant check-ins, while participants checked for available courts themselves. When all courts were occupied, participants typically waited nearby and watched for a space to become available. Without a clear waiting system, it can be difficult to determine who arrived first or whose turn should come next. Although the existing schedule provides useful information about activities, the current process still relies heavily on participants checking court availability and managing their waiting order themselves.

### Comparison and Opportunity for Improvement

The current SFU Drop-In Sports Schedule provides a convenient way for participants to find activities and plan their visits. Checking courts in person also allows participants to directly observe whether a space is occupied. However, these approaches have limitations. The online schedule focuses on activity times and locations rather than individual court availability or the number of people waiting, while checking in person requires participants to travel to the gym without knowing whether they will be able to play. Based on our experience with drop-in badminton, the lack of a clearly organized waiting queue can also make it difficult for participants to know who arrived first or whose turn should come next.

SFU RecQ aims to address these limitations by combining court availability information with an organized on-site waiting system. Recreation staff will update the status of each court through an administrator interface whenever its availability changes, allowing participants to view the latest recorded information and current waiting counts remotely. To join a queue, participants must arrive at the gym and complete an on-site check-in with staff, who will register them in a first-in, first-out (FIFO) queue. Once registered, participants can track their queue positions through the application instead of continuously watching for an available court. When a court becomes available, the system will notify the next participant by email and allow three minutes for staff-confirmed turn confirmation. If the participant does not return within the time limit, the opportunity will automatically pass to the next person in line. This approach aims to preserve the flexibility of drop-in sports while making court access more organized, transparent, and fair.

## Proposed Application and Improvements

### Regular User Workflow

SFU RecQ will be a browser-based application that allows eligible drop-in sports participants to check court availability and view current waiting conditions. After logging in, users will be able to browse sports activities, select a gym and a drop-in session, and view the status of individual courts or playing areas. These statuses will be updated by Recreation staff whenever court availability changes. Users will also be able to see the number of people waiting for each session before deciding whether to visit the gym.

To join a waiting queue, participants must first arrive at the gym and complete an on-site check-in with Recreation staff. If a court is available and no one is waiting, participants can use it directly. Otherwise, staff will register them in a single first-in, first-out (FIFO) queue for the selected gym, sport, and drop-in session. The system will record the order in which participants join the queue, ensuring that those who join earlier receive priority. Once registered, users will be able to view the current queue status and track their own position through the application. Their position will update as the queue moves forward, allowing them to wait without constantly watching for an available court.

When a court becomes available, the system will assign it to the participant at the front of the queue and send an email notification. The participant will then have three minutes from the time of assignment to return and have their turn confirmed by Recreation staff. If the participant fails to complete the confirmation before the deadline, the system will automatically offer the court to the next person in line. Participants can also leave the queue if they no longer wish to wait. All waiting queues will close at the end of their corresponding drop-in sessions, preventing queue positions from carrying over to future sessions.

### Administrative Workflow

SFU Recreation staff will manage facility availability and waiting queues through a dedicated administrator dashboard. After logging in with an authorized staff account, they will be able to view individual courts and playing areas, monitor their current statuses, and manually update availability whenever conditions change. For example, when participants finish using a court, staff will mark it as available through the dashboard. If there are participants waiting, the system will automatically assign the court to the next person in the queue. This approach allows staff to maintain accurate facility information while the application handles queue management and court allocation.

Staff will also be responsible for checking in participants when they arrive at the gym. After verifying their arrival, staff can allow participants to use an available court directly if no one is waiting. Otherwise, staff will register them in a single first-in, first-out (FIFO) queue for the corresponding gym, sport, and drop-in session. The system will record the order in which participants join, allowing staff to monitor the waiting list and track each participant's position. When a court becomes available, the system will assign it to the participant at the front of the queue and send an email notification. Staff will then confirm whether the participant returns to claim the court within three minutes. If the participant does not return before the deadline, the system will automatically offer the court to the next person in line.

In addition, staff will be able to close unavailable courts, remove invalid queue entries, and handle exceptional situations when necessary. However, they will not be able to arbitrarily change the normal waiting order. The system will also close the corresponding waiting queue when a drop-in session ends. By combining staff-controlled court availability updates with automatic FIFO queue management, SFU RecQ aims to provide a more organized and transparent process while helping Recreation staff manage facility usage more efficiently.

### Queue Fairness and Check-in Rules

To ensure fair access to drop-in sports facilities, SFU RecQ will use a single on-site first-in, first-out (FIFO) waiting queue for each gym, sport, and drop-in session. Participants must arrive at the gym and complete an on-site check-in with Recreation staff before joining the queue. Staff will register participants in the system, which will record their joining order and assign queue positions accordingly. When a court becomes available, the participant at the front of the queue will receive the next opportunity to use it. This ensures that participants are served in the order they joined, rather than having to compete for newly available courts.

To maintain the flexibility of drop-in sports, participants will not be able to join waiting queues remotely or reserve a specific court for a future time. They can check court availability and the current number of people waiting before visiting the gym, but joining the queue requires an in-person check-in. Once registered, participants will be able to view their queue positions and track the waiting progress through the application. When Recreation staff mark a court as available, the system will assign it to the next participant in line and send an email notification. The participant will have three minutes from the time of assignment to return and have their turn confirmed by Recreation staff.

If a participant fails to complete the turn confirmation within three minutes, their opportunity will expire, and the system will automatically offer the court to the next person in the FIFO queue. Staff will confirm successful turns through the administrator dashboard, while the system will maintain the waiting order and update queue positions as participants move forward. All waiting queues will close at the end of their corresponding drop-in sessions, preventing queue positions from carrying over to future sessions. By requiring on-site registration and maintaining a clear waiting order, SFU RecQ aims to make court access more organized, transparent, and fair while preserving the nature of drop-in sports.

## Project Scope and Major Epics

### Epic 1: User Authentication and Role Management

Epic 1 focuses on user authentication and role management. The system will support two types of users: Recreation staff and participants. Users will be able to create accounts, sign in, sign out, and manage basic profile information. Authorized staff accounts will be able to access the administration dashboard, while participant accounts will be able to use the facility discovery, availability, and queue features.

### Epic 2: Facility Discovery and Availability

Epic 2 allows users to browse SFU's drop-in sports sessions and check court availability before visiting the gym. Users can select a sport, gym, and session to view the status of individual courts and the number of people currently waiting. Court statuses will be updated by Recreation staff, while waiting counts will come from the queue management system. If a facility has a long waiting queue, users can also search for nearby alternative sports facilities through the Geoapify Places API. The application will display their names, addresses, and distances from the selected SFU facility. However, the availability of these alternative facilities cannot be guaranteed. Users must still check in with Recreation staff in person to join a queue at SFU.

### Epic 3: Administrative Facility Management

Epic 3 focuses on developing an administrator dashboard for SFU Recreation staff to manage courts and playing areas during drop-in sports sessions. Staff can view and manually update the status of each court when it becomes available or occupied. They can also close or reopen courts when needed. These changes will be shown to regular users so they can see updated court availability. The dashboard will also allow staff to view active waiting queues, the number of people waiting, and their current positions.

### Epic 4: On-Site FIFO Queue Management

Epic 4 focuses on managing the waiting queue of users who arrive on-site. After an in-person check-in with Recreation staff, users are registered into a FIFO queue for their selected session. They can then track their queue position in real time using the application. While waiting, users may answer interactive trivia questions, which will be implemented using the Open Trivia DB API.

### Epic 5: Check-in, Notifications, and Automatic Turn Management

When a court becomes available, the system will notify the first participant in the queue and provide a three-minute window for staff-confirmed turn confirmation. If the participant does not confirm the turn on time, the system will automatically skip that participant and notify the next person through the Brevo Transactional Email API.

## External Web REST API Integration

### 1. Brevo Transactional Email API

#### Selected API and Project Use Case

We plan to use the Brevo Transactional Email API as the external web REST API for our application. When a recreation staff member marks a court or activity space as released, the system will select the next eligible participant based on the queue rules and begin a three-minute check-in window. At the same time, the Spring Boot backend will send an HTTP POST request to the Brevo API to email the participant that a space is available and that they must check in within three minutes. This integration supports the main queue workflow by reducing missed turns and helping the queue move more efficiently. If the email service is temporarily unavailable, the queue will continue to operate, and the participant will still be able to see their status and countdown in the application.

#### Integration and Feasibility

The Brevo API will be integrated into the Spring Boot backend. When a facility becomes available, the application will first select the next participant according to the queue rules and start the three-minute check-in period. The backend will then send an HTTPS POST request to the Brevo API containing the participant's email address, the email subject, and the notification message. The API key will be stored as an environment variable on Render instead of being included in the source code or GitHub repository.

This integration is feasible because it requires only one main API endpoint and does not control the queue logic itself. Our application will continue to manage facility availability, queue order, spot allocation, and the check-in timer. Brevo will only be responsible for sending the notification email. The team can test the API with a small proof of concept before completing the integration in Iteration 3. If the API request fails, the queue will continue normally, and the selected participant can still view the notification and countdown within the application.

### 2. Open Trivia DB API

#### Selected API and Project Use Case

The project will use the Open Trivia DB API to provide short multiple-choice trivia questions for users waiting in the on-site queue. After a user checks in and is added to the queue by staff, they can answer trivia questions while viewing their current queue position. The trivia feature will not affect queue order or facility allocation.

#### Integration and Feasibility

The application will send HTTP requests to the Open Trivia DB API and receive questions and answers in JSON format. The API is free and does not require an API key. Session tokens can also be used to reduce repeated questions. This feature is independent of the main queue system, so if the API is temporarily unavailable, the trivia section can be hidden without affecting the queue.

### 3. Geoapify Places API

#### Selected API and Project Use Case

The project will use the Geoapify Places API to show nearby alternative sports facilities when the selected SFU facility has a long queue. The application will search around the selected facility and display a small number of nearby sports centres, sports halls, gyms, or swimming pools. Each result may include the facility name, address, and distance. The application will clearly state that availability at these alternative facilities is not guaranteed.

#### Integration and Feasibility

The backend will send an HTTP request to the Geoapify Places API using the selected facility's coordinates, a search radius, and relevant sports categories. The API key will be stored securely as an environment variable. Geoapify provides a free plan with up to 3,000 credits per day and does not require a credit card. To reduce API usage, the application will return only a few nearby results and may temporarily cache them. Because the search uses the fixed location of the facility, the application does not need to collect the user's personal location. If the API is unavailable, the alternative-facility section can be disabled without affecting the main availability and queue features.

## Team Background and Responsibilities

### Team Experience and Strengths

Emily Tsou has studied Python and C++ in her previous courses, where she learned programming fundamentals, problem-solving, and data structures. Through CMPT 276, she has started exploring web development using Java, Spring Boot, and HTML. She enjoys learning by doing and figuring out how different parts of a program work together. While she is still gaining experience in web development, she hopes to improve her technical skills and contribute to building a practical and easy-to-use application with her teammates.

Jiawen Wang studied Python, C++, and fundamental programming concepts in previous courses. She gained basic experience with Java, HTML, CSS, JavaScript, Spring Boot, Git, and GitHub through coursework in CMPT 276. She has also contributed to identifying user needs, discussing project requirements, and planning the main features of SFU RecQ.

Dmitriy Petrichshev has experience with C++ programming, data structures and algorithm design through relevant coursework, as well as a basic understanding of Java, Spring Boot, HTML, CSS, JavaScript and Git and GitHub version control. He looks forward to contributing to the team project, while learning about professional-style group workflows and practices.

Zhengyang Gu has experience with Spring Boot, HTML, CSS, JavaScript, Git, GitHub, and deploying web applications on Render through previous coursework. He is also developing a local text search tool as a personal project. His strengths include identifying practical user needs, organizing feature requirements, and designing clear user workflows.

### Proposed Responsibilities and Collaboration

Each team member will take primary responsibility for one major epic. Elysia Anne Wimmer will lead Epic 1, Emily Tsou will lead Epic 2, Jiawen Wang will lead Epic 3, Dmitriy Petrichshev will lead Epic 4, and Zhengyang Gu will lead Epic 5. Emily and Jiawen's developing web development experience supports the user-facing and administrative interfaces in Epics 2 and 3. Dmitriy's background in data structures and algorithms is relevant to the FIFO queue logic in Epic 4, while Zhengyang's experience with Spring Boot and web deployment supports the notification and automatic turn-management work in Epic 5.

Although each member will lead one epic, the features will be developed collaboratively. The team will agree on shared data structures and interfaces, use separate Git branches, review pull requests, and test connected features together. Regular meetings and group communication will be used to report progress, discuss problems, and coordinate work across iterations.

## Feasibility and Development Plan

The project is feasible because it will use technologies introduced through the team's coursework, including Spring Boot, HTML, CSS, JavaScript, Git, and GitHub. Facility availability will be updated manually by Recreation staff, so the application will not require sensors or access to SFU's internal systems. The external API features will remain separate from the core queue logic, allowing the essential availability and queue workflows to continue if an API is temporarily unavailable.

Development will be divided across three iterations. During Iteration 1, the team will implement authentication, establish shared data models and interfaces, and create an initial version of each epic. Iteration 2 will add the core facility status, administrator, FIFO queue, and three-minute turn-management workflows. Iteration 3 will integrate the Brevo, Open Trivia DB, and Geoapify APIs, followed by end-to-end testing, error handling, deployment, and usability improvements.
