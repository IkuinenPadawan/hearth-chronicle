# Hearth Chronicle

Hearth Chronicle is a full-stack web application for managing family calendar self-hosted. It allows users to view, add, update, and delete events on a calendar, with a backend built using Clojure and a frontend built with Reagent and FullCalendar.

## Features

- View events in a monthly calendar view
- Add, update, and delete events
- Interactive calendar with date and event click handlers
- Responsive user interface built with Reagent
- API backend built using Clojure and Ring
- PostgreSQL database for event storage

## Future Features
- Users and authentication
- Beautiful modern and minimalistic UI
- Containerization for ease of use
- Serving the whole app from backend
- Mobile support
- Event categories

## Technologies Used

### Frontend:
- [Reagent](https://reagent-project.github.io/) for building the user interface
- [FullCalendar](https://fullcalendar.io/) for interactive calendar functionality
- [cljs-http](https://github.com/r0man/cljs-http) for HTTP requests
- [Core.async](https://clojure.github.io/core.async/) for handling asynchronous operations

### Backend:
- [Clojure](https://clojure.org/) for the backend logic
- [Ring](https://ring-clojure.github.io/ring/) for web server and routing
- [Compojure](https://github.com/weavejester/compojure) for routing and API endpoints
- [HugSQL](https://github.com/juxt/hugsql) for SQL query management
- [PostgreSQL](https://www.postgresql.org/) for data storage

## Start Developing

### Prerequisites

Ensure you have the following tools installed:

- [Java](https://www.java.com/en/download/)
- [Leiningen](https://leiningen.org/)
- [Node.js](https://nodejs.org/)
- [npm](https://www.npmjs.com/)
- [Docker](https://docs.docker.com/get-started/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)

### Backend Setup

1. Clone the repository:

    ```bash
    git clone https://github.com/yourusername/hearth-chronicle.git
    cd hearth-chronicle
    ```

2. Run the database container

```bash
    docker compose up
```

3. Install the necessary Clojure dependencies:

    ```bash
    lein deps
    ```
4. Run migrations

```bash
    lein migratus migrate
```

5. Run the backend server:

    ```bash
    lein run
    ```

   The backend will be running at `http://localhost:8081`.

### Frontend Setup

1. Install Node.js dependencies:

    ```bash
    npm install
    ```

2. Build the frontend using Shadow-CLJS:

    ```bash
    npx shadow-cljs watch app
    ```

3. The frontend will be available at `http://localhost:3000`.
