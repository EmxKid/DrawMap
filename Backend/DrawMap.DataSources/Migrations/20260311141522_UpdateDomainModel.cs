using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace DrawMap.DataSources.Migrations
{
    /// <inheritdoc />
    public partial class UpdateDomainModel : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "Locations",
                table: "Routes");

            migrationBuilder.DropColumn(
                name: "Data",
                table: "Photos");

            migrationBuilder.DropColumn(
                name: "Location",
                table: "Photos");

            migrationBuilder.AddColumn<DateTime>(
                name: "EndTime",
                table: "Routes",
                type: "timestamp with time zone",
                nullable: false,
                defaultValue: new DateTime(1, 1, 1, 0, 0, 0, 0, DateTimeKind.Unspecified));

            migrationBuilder.AddColumn<DateTime>(
                name: "StartTime",
                table: "Routes",
                type: "timestamp with time zone",
                nullable: false,
                defaultValue: new DateTime(1, 1, 1, 0, 0, 0, 0, DateTimeKind.Unspecified));

            migrationBuilder.AddColumn<double>(
                name: "TotalDistance",
                table: "Routes",
                type: "double precision",
                nullable: false,
                defaultValue: 0.0);

            migrationBuilder.AddColumn<string>(
                name: "LocationId",
                table: "Photos",
                type: "text",
                nullable: true);

            migrationBuilder.CreateTable(
                name: "Locations",
                columns: table => new
                {
                    Id = table.Column<string>(type: "text", nullable: false),
                    Longitude = table.Column<double>(type: "double precision", nullable: false),
                    Latitude = table.Column<double>(type: "double precision", nullable: false),
                    Timestamp = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    VisitFrequency = table.Column<int>(type: "integer", nullable: false),
                    RouteId = table.Column<string>(type: "text", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Locations", x => x.Id);
                    table.ForeignKey(
                        name: "FK_Locations_Routes_RouteId",
                        column: x => x.RouteId,
                        principalTable: "Routes",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateIndex(
                name: "IX_Locations_RouteId",
                table: "Locations",
                column: "RouteId");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "Locations");

            migrationBuilder.DropColumn(
                name: "EndTime",
                table: "Routes");

            migrationBuilder.DropColumn(
                name: "StartTime",
                table: "Routes");

            migrationBuilder.DropColumn(
                name: "TotalDistance",
                table: "Routes");

            migrationBuilder.DropColumn(
                name: "LocationId",
                table: "Photos");

            migrationBuilder.AddColumn<string>(
                name: "Locations",
                table: "Routes",
                type: "jsonb",
                nullable: true);

            migrationBuilder.AddColumn<byte[]>(
                name: "Data",
                table: "Photos",
                type: "bytea",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "Location",
                table: "Photos",
                type: "jsonb",
                nullable: false,
                defaultValue: "");
        }
    }
}
