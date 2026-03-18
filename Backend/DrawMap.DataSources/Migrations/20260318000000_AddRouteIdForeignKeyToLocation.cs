using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace DrawMap.DataSources.Migrations
{
    /// <inheritdoc />
    public partial class AddRouteIdForeignKeyToLocation : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            // Добавляем внешний ключ для RouteId в таблице Locations
            migrationBuilder.CreateIndex(
                name: "IX_Locations_RouteId",
                table: "Locations",
                column: "RouteId");

            migrationBuilder.AddForeignKey(
                name: "FK_Locations_Routes_RouteId",
                table: "Locations",
                column: "RouteId",
                principalTable: "Routes",
                principalColumn: "Id",
                onDelete: ReferentialAction.Cascade);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_Locations_Routes_RouteId",
                table: "Locations");

            migrationBuilder.DropIndex(
                name: "IX_Locations_RouteId",
                table: "Locations");
        }
    }
}
