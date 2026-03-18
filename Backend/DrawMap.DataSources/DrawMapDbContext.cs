using DrawMap.DataSources.Configurations;
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

        modelBuilder.ApplyConfiguration(new PhotoConfiguration());
        modelBuilder.ApplyConfiguration(new LocationConfiguration());
        modelBuilder.ApplyConfiguration(new RouteConfiguration());
    }
}
