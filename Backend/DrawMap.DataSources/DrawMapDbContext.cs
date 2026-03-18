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

    public DbSet<Route> Routes { get; set; }
    public DbSet<Photo> Photos { get; set; }
    public DbSet<Location> Locations { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        modelBuilder.ApplyConfiguration(new PhotoConfiguration());
        modelBuilder.ApplyConfiguration(new LocationConfiguration());
        modelBuilder.ApplyConfiguration(new RouteConfiguration());
    }
}
