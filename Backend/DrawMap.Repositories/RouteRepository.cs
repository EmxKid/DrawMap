using DrawMap.DataSources;
using DrawMap.Domain;
using Microsoft.EntityFrameworkCore;

namespace DrawMap.Repositories;

public class RouteRepository : IRouteRepository
{
    private readonly DrawMapDbContext _context;

    public RouteRepository(DrawMapDbContext context)
    {
        _context = context;
    }

    public async Task<string> AddRoute(Route route)
    {
        _context.Routes.Add(route);
        await _context.SaveChangesAsync();
        return route.Id ?? string.Empty;
    }

    public async Task<Route?> GetRoute(string routeId)
    {
        return await _context.Routes
            .Include(r => r.Photos)
            .Include(r => r.Locations)
            .FirstOrDefaultAsync(r => r.Id == routeId);
    }

    public async Task<Route?> UpdateRoute(string routeId, Route route)
    {
        var existing = await _context.Routes
            .Include(r => r.Photos)
            .Include(r => r.Locations)
            .FirstOrDefaultAsync(r => r.Id == routeId);

        if (existing is null)
            return null;

        existing.TotalDistance = route.TotalDistance;
        existing.StartTime = route.StartTime;
        existing.EndTime = route.EndTime;
        existing.Locations = route.Locations;
        existing.Photos = route.Photos;

        await _context.SaveChangesAsync();
        return existing;
    }

    public async Task<bool> DeleteRoute(string routeId)
    {
        var existing = await _context.Routes.FindAsync(routeId);
        if (existing is null)
            return false;

        _context.Routes.Remove(existing);
        await _context.SaveChangesAsync();
        return true;
    }
}
