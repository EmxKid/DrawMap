using DrawMap.Domain;
using Microsoft.EntityFrameworkCore;

namespace DrawMap.DataSources;

public class DrawMapDbContext : DbContext
{
    public DrawMapDbContext(DbContextOptions<DrawMapDbContext> options)
        : base(options)
    {
    }

    public DbSet<Route> Routes => Set<Route>();
    public DbSet<Photo> Photos => Set<Photo>();
    public DbSet<Location> Locations => Set<Location>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        modelBuilder.Entity<Location>(entity =>
        {
            entity.HasKey(l => l.Id);

            entity.Property(l => l.Id)
                .IsRequired();
        });

        modelBuilder.Entity<Route>(entity =>
        {
            entity.HasKey(r => r.Id);

            entity.Property(r => r.Id)
                .IsRequired();

            entity.HasMany(r => r.Locations)
                .WithOne()
                .OnDelete(DeleteBehavior.Cascade);

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

            entity.Property(p => p.LocationId)
                .IsRequired(false);
        });
    }
}
