package sovereignchessml;

import static spark.Spark.*;
import com.google.gson.*;

public class ChessBackend {

    public static void main(String[] args) {
        Gson gson = new Gson();

        // Start server on port 4567
        port(4567);

        // Enable CORS
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });
        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));

        // Endpoint to handle move requests
        post("/api/move", (req, res) -> {
            JsonObject jsonRequest = JsonParser.parseString(req.body()).getAsJsonObject();

            // Extract the board
            Board inputBoard = gson.fromJson(jsonRequest.get("board"), Board.class);

            // Extract the move
            JsonObject moveJson = jsonRequest.get("move").getAsJsonObject();
            int row = moveJson.get("row").getAsInt();
            int col = moveJson.get("col").getAsInt();
            Square square = new Square(row, col);

            // Process the move and board
            Board updatedBoard = inputBoard.processBoard(inputBoard, square);

            // Return the updated board
            return gson.toJson(updatedBoard);
        });
    }
}
