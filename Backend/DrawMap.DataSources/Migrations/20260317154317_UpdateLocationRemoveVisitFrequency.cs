using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace DrawMap.DataSources.Migrations
{
    /// <inheritdoc />
    public partial class UpdateLocationRemoveVisitFrequency : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_Photos_Routes_RouteId",
                table: "Photos");

            migrationBuilder.DropIndex(
                name: "IX_Photos_RouteId",
                table: "Photos");

            migrationBuilder.DropColumn(
                name: "RouteId",
                table: "Photos");

            migrationBuilder.DropColumn(
                name: "VisitFrequency",
                table: "Locations");

            migrationBuilder.CreateIndex(
                name: "IX_Photos_LocationId",
                table: "Photos",
                column: "LocationId",
                unique: true);

            migrationBuilder.AddForeignKey(
                name: "FK_Photos_Locations_LocationId",
                table: "Photos",
                column: "LocationId",
                principalTable: "Locations",
                principalColumn: "Id");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_Photos_Locations_LocationId",
                table: "Photos");

            migrationBuilder.DropIndex(
                name: "IX_Photos_LocationId",
                table: "Photos");

            migrationBuilder.AddColumn<string>(
                name: "RouteId",
                table: "Photos",
                type: "text",
                nullable: false,
                defaultValue: "");

            migrationBuilder.AddColumn<int>(
                name: "VisitFrequency",
                table: "Locations",
                type: "integer",
                nullable: false,
                defaultValue: 0);

            migrationBuilder.CreateIndex(
                name: "IX_Photos_RouteId",
                table: "Photos",
                column: "RouteId");

            migrationBuilder.AddForeignKey(
                name: "FK_Photos_Routes_RouteId",
                table: "Photos",
                column: "RouteId",
                principalTable: "Routes",
                principalColumn: "Id",
                onDelete: ReferentialAction.Cascade);
        }
    }
}
