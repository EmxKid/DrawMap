using System.Text.Json;
using DrawMap.Domain;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.ChangeTracking;
using Microsoft.EntityFrameworkCore.Storage.ValueConversion;

namespace DrawMap.DataSources;

public class DrawMapDbContext : DbContext
{
    public DrawMapDbContext(DbContextOptions<DrawMapDbContext> options)
        : base(options)
    {
    }

    public DbSet<Route> Routes => Set<Route>();
    public DbSet<Photo> Photos => Set<Photo>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        var locationConverter = new ValueConverter<Location, string>(
            loc => JsonSerializer.Serialize(loc, (JsonSerializerOptions?)null),
            json => JsonSerializer.Deserialize<Location>(json, (JsonSerializerOptions?)null));

        var locationsConverter = new ValueConverter<Location[]?, string?>(
            locs => locs == null ? null : JsonSerializer.Serialize(locs, (JsonSerializerOptions?)null),
            json => json == null ? null : JsonSerializer.Deserialize<Location[]>(json, (JsonSerializerOptions?)null));

        var locationsComparer = new ValueComparer<Location[]?>(
            (a, b) => JsonSerializer.Serialize(a, (JsonSerializerOptions?)null) ==
                      JsonSerializer.Serialize(b, (JsonSerializerOptions?)null),
            v => v == null ? 0 : JsonSerializer.Serialize(v, (JsonSerializerOptions?)null).GetHashCode(),
            v => v == null ? null : JsonSerializer.Deserialize<Location[]>(
                     JsonSerializer.Serialize(v, (JsonSerializerOptions?)null), (JsonSerializerOptions?)null));

        modelBuilder.Entity<Route>(entity =>
        {
            entity.HasKey(r => r.Id);

            entity.Property(r => r.Id)
                .IsRequired();

            // Массив Location[] хранится как JSON-колонка
            entity.Property(r => r.Locations)
                .HasConversion(locationsConverter!, locationsComparer!)
                .HasColumnType("jsonb")
                .IsRequired(false);

            entity.HasMany(r => r.Photos)
                .WithOne()
                .HasForeignKey(p => p.RouteId)
                .OnDelete(DeleteBehavior.Cascade);
        });

        modelBuilder.Entity<Photo>(entity =>
        {
            entity.HasKey(p => p.Id);

            entity.Property(p => p.Id)
                .IsRequired();

            entity.Property(p => p.RouteId)
                .IsRequired();

            // Location — struct, хранится как JSON-колонка
            entity.Property(p => p.Location)
                .HasConversion(locationConverter)
                .HasColumnType("jsonb");

            entity.Property(p => p.Data)
                .IsRequired(false);
        });
    }
}
